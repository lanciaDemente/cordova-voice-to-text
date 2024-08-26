package com.test.cordova.plugin;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VoiceToTextPlugin extends CordovaPlugin implements RecognitionListener {
  private static final int REQUEST_CODE_RECOGNIZER = 2001;
  private static final int REQUEST_CODE_MIC_PERMISSION = 2002;

  private static final String IS_RECOGNITION_AVAILABLE = "isRecognitionAvailable";
  private static final String IS_MIC_PERMISSION_GRANTED = "isMicPermissionGranted";
  private static final String REQUEST_MIC_PERMISSION = "requestMicPermission";
  private static final String IS_RECOGNIZING = "isRecognizing";
  private static final String START_LISTENING = "startListening";
  private static final String STOP_LISTENING = "stopListening";
  private static final String CANCEL_SPEECH = "cancelSpeech";
  private static final String DESTROY_SPEECH = "destroySpeech";

  private static final int MAX_RESULTS = 5;
  
  private CallbackContext callbackContext;
  private Activity activity;
  private Context context;
  private View view;

  private String locale = null;
  private SpeechRecognizer recognizer;
  private boolean isRecognizing = false;

  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);

    activity = cordova.getActivity();
    context = webView.getContext();
    view = webView.getView();
  }

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callback) throws JSONException {
    this.callbackContext = callback;
    
    String errorMessage = "Method not found";

    try {
      if (IS_RECOGNITION_AVAILABLE.equals(action)) {
        isRecognitionAvailable();
        return true;
      }

      if (IS_MIC_PERMISSION_GRANTED.equals(action)) {
        isMicPermissionGranted();
        return true;
      }

      if (REQUEST_MIC_PERMISSION.equals(action)) {
        requestMicPermission();
        return true;
      }

      if (IS_RECOGNIZING.equals(action)) {
        isRecognizing();
        return true;
      }

      if (START_LISTENING.equals(action)) {
        startListening(args);
        return true;
      }

      if (STOP_LISTENING.equals(action)) {
        stopListening();
        return true;
      }

      if (CANCEL_SPEECH.equals(action)) {
        cancelSpeech();
        return true;
      }

      if (DESTROY_SPEECH.equals(action)) {
        destroySpeech();
        return true;
      }
      
    } catch (Exception e) {
      e.printStackTrace();
      errorMessage = e.getMessage();
    }

    callbackContext.error(errorMessage);
    return false;
  }

  private void isRecognitionAvailable() {
    final VoiceToTextPlugin self = this;
    Handler mainHandler = new Handler(this.activity.getMainLooper());
    mainHandler.post(new Runnable() {
      @Override
      public void run() {
        try {
          Boolean isRecognitionAvailable = SpeechRecognizer.isRecognitionAvailable(self.activity);
          callbackContext.success(isRecognitionAvailable ? 1 : 0);
        } catch(Exception e) {
          callbackContext.error(e.getMessage());
        }
      }
    });
  }

  private boolean _isMicPermissionGranted() {
    return cordova.hasPermission(Manifest.permission.RECORD_AUDIO);
  }

  private void isMicPermissionGranted() {
    if (_isMicPermissionGranted()) {
      callbackContext.success(1);
    } else {
      callbackContext.success(0);
    }
  }

  private void requestMicPermission() {
    cordova.requestPermission(this, REQUEST_CODE_MIC_PERMISSION, Manifest.permission.RECORD_AUDIO);
    // callback gestita da onRequestPermissionResult
  }

  public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
    if (requestCode == REQUEST_CODE_MIC_PERMISSION) {
      Boolean ok = true;
      for(int r:grantResults) {
        ok = r != PackageManager.PERMISSION_DENIED;
      }
      
      this.callbackContext.success(ok ? 1 : 0);
    }
    this.callbackContext.error("Permission not valid.");
}

  private String _getLocale(String locale) {
    if (locale != null && !locale.equals("")) {
      return locale;
    }
    return Locale.getDefault().toString();
  }

  private void isRecognizing() {
    callbackContext.success(isRecognizing ? 1 : 0);
  }

  private void _startListening(final String engine, final String langModel, final String locale, final int matches, 
    final String prompt, final Boolean showPartial, final Boolean showPopup) {
    
    if (recognizer != null) {
      recognizer.destroy();
      recognizer = null;
    }
    
    Boolean useGooglePopup = showPopup;

    switch (engine) {
      case "GOOGLE": {
        recognizer = SpeechRecognizer.createSpeechRecognizer(activity, ComponentName.unflattenFromString("com.google.android.googlequicksearchbox/com.google.android.voicesearch.serviceapi.GoogleRecognitionService"));
        useGooglePopup = true; // Con Google engine occorre l'utilizzo del popup altrimenti non si ha una corretta gestione degli errori
        break;
      }
      default:
        recognizer = SpeechRecognizer.createSpeechRecognizer(activity);
        break;
    }
    
    recognizer.setRecognitionListener(this);

    final Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

    switch (langModel) {
      case RecognizerIntent.LANGUAGE_MODEL_FREE_FORM:
      intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
      break;
    case RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH:
      intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
      break;
    default:
      intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
      break;
    }

    intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, matches);
    intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, showPartial);
    //intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1000);
    //intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, extras.intValue());
    //intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, extras.intValue());
    
    if (prompt != null) {
      intent.putExtra(RecognizerIntent.EXTRA_PROMPT, prompt);
    }

    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, _getLocale(this.locale));

    if (useGooglePopup) {
      cordova.startActivityForResult(this, intent, REQUEST_CODE_RECOGNIZER);
    } else {
      //view.post(new Runnable() {
      //  @Override
      //  public void run() {
      //    recognizer.startListening(intent);
      //  }
      //});
      recognizer.startListening(intent);
    }
  }

  private void _startListeningWithPermissions(final String engine, final String langModel, final String locale, final int matches, 
  final String prompt, final Boolean showPartial, final Boolean showPopup) {
    
    this.locale = locale;

    Handler mainHandler = new Handler(this.activity.getMainLooper());
    mainHandler.post(new Runnable() {
      @Override
      public void run() {
        try {
          _startListening(engine, langModel, locale, matches, prompt, showPartial, showPopup);
          isRecognizing = true;

          // Non viene chiusa la callback
          // verrà chiusa dagli eventi del RecognitionListener
          // callbackContext.success(1);

        } catch (Exception e) {
          callbackContext.error(e.getMessage());
        }
      }
    });
  }
  
  private void startListening(final JSONArray args) {
    String engine = args.optString(0, "SYSTEM");
    String langModel = args.optString(1, "DEFAULT");
    String lang = args.optString(2);
    int matches = args.optInt(3, MAX_RESULTS);
    String prompt = args.optString(4);
    Boolean showPartial = args.optBoolean(5, false);
    Boolean showPopup = args.optBoolean(6, false);

    if (lang == null || lang.isEmpty() || lang.equals("null")) {
      lang = Locale.getDefault().toString();
    }

    if (prompt == null || prompt.isEmpty() || prompt.equals("null")) {
      prompt = null;
    }

    if (!_isMicPermissionGranted()) {
      if (activity != null) {
        cordova.requestPermission(this, REQUEST_CODE_MIC_PERMISSION, Manifest.permission.RECORD_AUDIO);
        if (_isMicPermissionGranted()) {
          _startListeningWithPermissions(engine, langModel, lang, matches, prompt, showPartial, showPopup);
        }
      }
      return;
    }

    _startListeningWithPermissions(engine, langModel, lang, matches, prompt, showPartial, showPopup);
  }

  private void stopListening() {
    Handler mainHandler = new Handler(this.activity.getMainLooper());
    mainHandler.post(new Runnable() {
      @Override
      public void run() {
        try {
          if (recognizer != null) {
            recognizer.stopListening();
          }

          isRecognizing = false;
          callbackContext.success(1);

        } catch(Exception e) {
          callbackContext.error(e.getMessage());
        }
      }
    });
  }

  private void cancelSpeech() {
    Handler mainHandler = new Handler(this.activity.getMainLooper());
    mainHandler.post(new Runnable() {
      @Override
      public void run() {
        try {
          if (recognizer != null) {
            recognizer.cancel();
          }

          isRecognizing = false;
          callbackContext.success(1);

        } catch(Exception e) {
          callbackContext.error(e.getMessage());
        }
      }
    });
  }

  private void destroySpeech() {
    Handler mainHandler = new Handler(this.activity.getMainLooper());
    mainHandler.post(new Runnable() {
      @Override
      public void run() {
        try {
          if (recognizer != null) {
            recognizer.destroy();
          }

          recognizer = null;
          isRecognizing = false;
          callbackContext.success(1);

        } catch(Exception e) {
          callbackContext.error(e.getMessage());
        }
      }
    });
  }

  //

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_CODE_RECOGNIZER) {
      if (resultCode == Activity.RESULT_OK) {
        try {
          ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
          JSONArray jsonMatches = new JSONArray(matches);
          this.callbackContext.success(jsonMatches);
        } catch (Exception e) {
          e.printStackTrace();
          this.callbackContext.error(e.getMessage());
        }
      } else {
        this.callbackContext.error(Integer.toString(resultCode));
      }

      return;
    }
  }

  @Override
  public void onBeginningOfSpeech() { }

  @Override
  public void onBufferReceived(byte[] buffer) { }

  @Override
  public void onEndOfSpeech() {
    isRecognizing = false;
  }

  @Override
  public void onError(int errorCode) {
    isRecognizing = false;
    //
    String errorMessage = _getErrorText(errorCode);
    this.callbackContext.error(errorMessage);
  }

  @Override
  public void onEvent(int arg0, Bundle arg1) { }
 
  @Override
  public void onReadyForSpeech(Bundle arg0) { }
 
  @Override
  public void onPartialResults(Bundle results) { 
    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
    
    try {
      JSONArray matchesJSON = new JSONArray(matches);
      if (matches != null) {
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, matchesJSON);
        pluginResult.setKeepCallback(true);
        callbackContext.sendPluginResult(pluginResult);
      }
    } catch (Exception e) {
      e.printStackTrace();
      callbackContext.error(e.getMessage());
    }
  }
  
  @Override
  public void onResults(Bundle results) {
    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
    
    try {
      JSONArray jsonMatches = new JSONArray(matches);
      callbackContext.success(jsonMatches);
    } catch (Exception e) {
      e.printStackTrace();
      callbackContext.error(e.getMessage());
    }
  }

  @Override
  public void onRmsChanged(float rmsdB) { }

  private static String _getErrorText(int errorCode) {
    String message;

    switch (errorCode) {
      case SpeechRecognizer.ERROR_AUDIO:
        message = "Audio recording error";
        break;
      case SpeechRecognizer.ERROR_CLIENT:
        message = "Client side error";
        break;
      case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
        message = "Insufficient permissions";
        break;
      case SpeechRecognizer.ERROR_NETWORK:
        message = "Network error";
        break;
      case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
        message = "Network timeout";
        break;
      case SpeechRecognizer.ERROR_NO_MATCH:
        message = "No match";
        break;
      case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
        message = "RecognitionService busy";
        break;
      case SpeechRecognizer.ERROR_SERVER:
        message = "error from server";
        break;
      case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
        message = "No speech input";
        break;
      default:
        message = "Didn't understand, please try again.";
        break;
    }

    return message;
  }
}
