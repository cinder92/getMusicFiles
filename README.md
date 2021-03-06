# getMusicFiles

This plugin was created to get all music files with metadata in our devices Android (4.x / 5.x), not implemented yet in 6.0, can be use to create a Music Player for example.

# Why?
Because if we use a plugin to read device folders on internal / external storage, app with cordova will be to SLOW, so with this plugin we can get all music files quickly.

# Requiremets

- Android 4.x - 5.x
- Cordova 3.x - 5.x

First you need to set android permissions to your Androidmanifest.xml

```
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

# Features

This plugin returns a JSON array with following info

- Id : Song id (generated by Java)
- Album : Album name
- Title : Song title
- Cover : Cover song
- Blur : Blurred Cover song (for cool designs)
- Author : Author song
- Genre : Genre song
- Duration : Song duration (miliseconds)
- Path : Absolute path in External / Internal storage

# How to install?

```
cordova plugin add https://github.com/cinder92/getMusicFiles.git
```

# How to use?

```
cordova.plugins.getExtPath.coolMethod("getMusic",function(music){
  //this returns a JSON array so
  for(var i = 0; i < music.length; i++){
      console.log('Id: '+music[i].Id)
      console.log('Song : '+music[i].Title)
      console.log('Album : '+music[i].Album)
      console.log('Cover: '+music[i].Cover)
      console.log('Blur: '+music[i].Blur)
      console.log('Author: '+music[i].Author)
      console.log('Genre: '+music[i].Genre)
      console.log('Duration: '+music[i].Duration)
      console.log('Path: '+music[i].Path)
  }
},function(msg){
  console.log('Oops! there was an error => '+msg)
})
```

# TODO

- Fix issues with Android 6.0
- Get Cover Art from internet
- Add conditionals to generate Blur / Cover 
