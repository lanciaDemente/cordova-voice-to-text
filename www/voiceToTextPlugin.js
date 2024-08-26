module.exports = {
  isRecognitionAvailable: function(successCB, errorCB) {
    cordova.exec(successCB, errorCB, "VoiceToTextPlugin", "isRecognitionAvailable", []);
  },
  isMicPermissionGranted: function(successCB, errorCB) {
    cordova.exec(successCB, errorCB, "VoiceToTextPlugin", "isMicPermissionGranted", []);
  },
  requestMicPermission: function(successCB, errorCB) {
    cordova.exec(successCB, errorCB, "VoiceToTextPlugin", "requestMicPermission", []);
  },
  isRecognizing: function(successCB, errorCB) {
    cordova.exec(successCB, errorCB, "VoiceToTextPlugin", "isRecognizing", []);
  },
  startListening: function(options, successCB, errorCB) {
    options = options || {};
    cordova.exec(successCB, errorCB, "VoiceToTextPlugin", "startListening", [
      options.engine,
      options.langModel,
      options.lang,
      options.matches,
      options.prompt,
      options.showPartial,
      options.showPopup
    ]);
  },
  stopListening: function(successCB, errorCB) {
    cordova.exec(successCB, errorCB, "VoiceToTextPlugin", "stopListening", []);
  },
  cancelSpeech: function(successCB, errorCB) {
    cordova.exec(successCB, errorCB, "VoiceToTextPlugin", "cancelSpeech", []);
  },
  destroySpeech: function(successCB, errorCB) {
    cordova.exec(successCB, errorCB, "VoiceToTextPlugin", "destroySpeech", []);
  }
};
