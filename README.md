# cordova-voice-to-text

Plugin Android che permette di utilizzare le funzioni di speech recognition.

## Platforms

Android

## Installation

```
cordova plugin add cordova-plugin-voicetotext
```

## Usage

This plugin requires internet connection.

### isRecognitionAvailable

```
window.plugins.voiceToText.isRecognitionAvailable(
  function successCallback, 
  function errorCallback
)
```

Result of success callback is a `Boolean`.

### isMicPermissionGranted

```
window.plugins.voiceToText.isMicPermissionGranted(
  function successCallback, 
  function errorCallback
)
```

Result of success callback is a `Boolean`.

### requestMicPermission

Use this function to request mic permissions to user. You need mic permission before call startListening function.

```
window.plugins.voiceToText.requestMicPermission(
  function successCallback, 
  function errorCallback
)
```

Result of success callback is a `Boolean`.

### isRecognizing

```
window.plugins.voiceToText.isRecognizing(
  function successCallback, 
  function errorCallback
)
```

Result of success callback is a `Boolean`.

### startListening

```
window.plugins.voiceToText.startListening(
  object options,
  function successCallback, 
  function errorCallback
)
```

#### Options

```
options = {
  engine: string,
  langModel: string,
  lang: string,
  matches: int,
  prompt: string,
  showPartial: boolean,
  showPopup: boolean,
  minSpeechDuration: int
}
```

- engine: engine for speech recognition (default *SYSTEM*). If engine is *GOOGLE* a Google popup will appear.
- langModel: can be *LANGUAGE_MODEL_FREE_FORM* or *LANGUAGE_MODEL_WEB_SEARCH* (default *LANGUAGE_MODEL_FREE_FORM*)
- lang: used language for recognition (default *en-US*)
- matches: number of return matches (default *3*)
- prompt: displayed prompt of listener popup window (dedault *null*)
- showPartial: allow partial results to be returned (default *false*)
- showPopup: display listener popup window with prompt (default *false*)
- minSpeechDuration: min duration of recognition (default *2000ms*)

### stopListening

```
window.plugins.voiceToText.stopListening(
  function successCallback, 
  function errorCallback
)
```

Result of success callback is a `Boolean`.

### cancelSpeech

```
window.plugins.voiceToText.cancelSpeech(
  function successCallback, 
  function errorCallback
)
```

Result of success callback is a `Boolean`.

### destroySpeech

```
window.plugins.voiceToText.destroySpeech(
  function successCallback, 
  function errorCallback
)
```

Result of success callback is a `Boolean`.
