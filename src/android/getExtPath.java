package cordova.plugin.getExtPath;

import android.app.Activity;
import android.content.ContentResolver;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.Object;
import java.lang.Throwable;
import java.lang.Exception;
import java.io.IOException;
import java.io.FileNotFoundException;

import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.lang.Override;
import java.lang.String;
import java.util.ArrayList;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.util.Base64;
import android.content.Context;

/**
 * This class echoes a string called from JavaScript.
 */
public class getExtPath extends CordovaPlugin {

    private ArrayList<HashMap<String, String>> songsList = new  ArrayList<HashMap<String, String>>();

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("coolMethod")) {
            String message = args.getString(0);
            this.getAllSongs(callbackContext);
            return true;
        }
        return false;
    }

    private void getAllSongs(CallbackContext callbackContext){
        ContentResolver musicResolver = this.cordova.getActivity().getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if (musicCursor != null && musicCursor.moveToFirst()) {

            JSONArray jsonArray = new JSONArray();
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            JSONObject items = null;

            //variables para cuando no existen metadatos
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);

            try {


                do {
                    items = new JSONObject();
                    byte[] art;

                    //sin metadatos
                    long thisId = musicCursor.getLong(idColumn);
                    String thisPath = musicCursor.getString(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    //String thisName = musicCursor.getString(musicCursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                    String thisTitle = musicCursor.getString(titleColumn);
                    String thisArtist = musicCursor.getString(artistColumn);
                    String Duration = musicCursor.getString(musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION));

                    mmr.setDataSource(thisPath); //para obtener los metadatos

                    //con metadatos
                    String album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                    String author = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                    String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                    String genero = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
                    String encoded = "";
                    String blurred = "";
                    String encodedImage = "";
                    String blurImage = "";
                    art = mmr.getEmbeddedPicture();

                    if(album == null){
                        album = thisArtist;
                    }

                    if(author == null){
                        author = thisArtist;
                    }

                    if(title == null){
                        title = thisTitle;
                    }


                    // convert the byte array to a bitmap
                    if(art != null) {

                        Bitmap songImage = BitmapFactory.decodeByteArray(art, 0, art.length);
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        songImage.compress(Bitmap.CompressFormat.JPEG, 60, byteArrayOutputStream);
                        byte[] byteArray = byteArrayOutputStream.toByteArray();
                        //encoded = "data:image/jpeg;base64,"+Base64.encodeToString(byteArray, Base64.DEFAULT);
                        encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
                        //byte[] decodedString = Base64.decode(encoded, Base64.DEFAULT);
                        //Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                        //guardar archivo en el sistema
                        String pathtoImg = "";
                        byte[] imageByte = Base64.decode(encodedImage, Base64.DEFAULT);
                        try {
                            pathtoImg = Environment.getExternalStorageDirectory()+"/"+thisId+".jpg";
                            File filePath = new File(pathtoImg);
                            FileOutputStream fos = new FileOutputStream(filePath, true);
                            encoded = pathtoImg;
                            fos.write(imageByte);
                            fos.flush();
                            fos.close();
                        } catch(FileNotFoundException fnfe) { 
                            callbackContext.error(fnfe.getMessage());
                        } catch(IOException ioe){
                            callbackContext.error(ioe.getMessage());
                        }

                        //filtro gausiano
                        Blur blur = new Blur();
                        Context context = this.cordova.getActivity().getApplicationContext(); 
                        Bitmap blurimg = blur.fastblur(context,songImage,20);

                        //Bitmap songImages = BitmapFactory.decodeByteArray(art, 0, art.length);
                        ByteArrayOutputStream byteArrayOutputStreams = new ByteArrayOutputStream();
                        
                        blurimg.compress(Bitmap.CompressFormat.JPEG, 60, byteArrayOutputStreams);
                        byte[] byteArrays = byteArrayOutputStreams.toByteArray();
                        //encoded = "data:image/jpeg;base64,"+Base64.encodeToString(byteArray, Base64.DEFAULT);
                        blurImage = Base64.encodeToString(byteArrays, Base64.DEFAULT);
                        //byte[] decodedString = Base64.decode(encoded, Base64.DEFAULT);
                        //Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                        //guardar archivo en el sistema
                        byte[] imageBytes = Base64.decode(blurImage, Base64.DEFAULT);
                        try {
                            pathtoImg = Environment.getExternalStorageDirectory()+"/"+thisId+"-blur.jpg";
                            File filePath = new File(pathtoImg);
                            FileOutputStream fos = new FileOutputStream(filePath, true);
                            blurred = pathtoImg;
                            fos.write(imageBytes);
                            fos.flush();
                            fos.close();
                        } catch(FileNotFoundException fnfe) { 
                            callbackContext.error(fnfe.getMessage());
                        } catch(IOException ioe){
                            callbackContext.error(ioe.getMessage());
                        }
                        
                    }


                    items.put("Id", thisId);
                    items.put("Album", album);
                    items.put("Author", author);
                    items.put("Title", title);
                    items.put("Genre", genero);
                    items.put("Cover", encoded);
                    items.put("Blur", blurred);
                    items.put("Duration", Duration);
                    items.put("Path", thisPath);
                    jsonArray.put(items);

                } while (musicCursor.moveToNext());
                callbackContext.success(jsonArray);
                mmr.release();

            } catch (RuntimeException e) {
                callbackContext.error(e.toString());
                mmr.release();
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                callbackContext.error("JSON Error! "+e.getMessage());
                mmr.release();
            }
        }

        /*
        //get columns
        int titleColumn = musicCursor.getColumnIndex
          (android.provider.MediaStore.Audio.Media.TITLE);
        int idColumn = musicCursor.getColumnIndex
          (android.provider.MediaStore.Audio.Media._ID);
        int artistColumn = musicCursor.getColumnIndex
          (android.provider.MediaStore.Audio.Media.ARTIST);

        do {
          long thisId = musicCursor.getLong(idColumn);
          String thisTitle = musicCursor.getString(titleColumn);
          String thisArtist = musicCursor.getString(artistColumn);
          String thisPath = musicCursor.getString(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));

          HashMap<String, String> song = new HashMap<String,String>();
          song.put("title", thisTitle);
          song.put("path", thisPath);
          song.put("artist",thisArtist);

          // Adding each song to SongList
         songsList.add(song);
        } while (musicCursor.moveToNext());
        callbackContext.success(songsList.toString());
      }else{
        callbackContext.error("no hay música");
      }*/
      /*Uri allsongsuri =  MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
      String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

      String[] STAR = null;

      Cursor cursor = this.cordova.getActivity().getContentResolver().query(allsongsuri, null, null, null, selection);

      MediaMetadataRetriever mmr = new MediaMetadataRetriever();
      JSONObject items = new JSONObject();

      if (cursor != null) {
        if (cursor.moveToFirst()) {
          try {
            do {
              byte[] art;
              String song_name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
              int song_id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));

              String fullpath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
              String Duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));

              mmr.setDataSource(fullpath);
              String album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
              String author = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
              String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
              String genero = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);

              art = mmr.getEmbeddedPicture();
              Bitmap songImage = BitmapFactory.decodeByteArray(art, 0, art.length);
              ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
              songImage.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
              byte[] byteArray = byteArrayOutputStream.toByteArray();
              String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);

              items.put("Album", album);
              items.put("Author", author);
              items.put("Title", title);
              items.put("Name", song_name);
              items.put("Genre", genero);
              items.put("Cover", encoded);
              items.put("Duration", Duration);
              items.put("Path", fullpath);

              //Log.e(TAG, "Song Name ::"+song_name+" Song Id :"+song_id+" fullpath ::"+fullpath+" Duration ::"+Duration);

            } while (cursor.moveToNext());
          } catch (JSONException e) {
            // TODO Auto-generated catch block
            callbackContext.error(e.getMessage());
            mmr.release();
          }

        }
        cursor.close();
        callbackContext.success(items);
        mmr.release();
      }else{
        callbackContext.error("No hay musica :(");
        mmr.release();
      }*/
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        String baseDir = "";
        //if (message != null && message.length() > 0) {

        if ("getExternal".equals(message)) {

            if(new File("/storage/extSdCard/").exists())
            {
                baseDir+="/storage/extSdCard/";
                // Log.i("Sd Cardext Path",sdpath);
            }
            if(new File("/storage/sdcard1/").exists())
            {
                baseDir+="/storage/sdcard1/";
                //Log.i("Sd Card1 Path",sdpath);
            }
            if(new File("/storage/usbcard1/").exists())
            {
                baseDir+="/storage/usbcard1/";
                //Log.i("USB Path",sdpath);
            }

            if(new File("/storage/external_SD/").exists())
            {
                baseDir+="/storage/external_SD/";
                //Log.i("Sd Card0 Path",sdpath);
            }

            callbackContext.success(baseDir);
            //getInternal(callbackContext);
        }else{
            if("getInternal".equals(message)){
                baseDir +=  Environment.getExternalStorageDirectory().getAbsolutePath();
                baseDir += "/";
                callbackContext.success(baseDir);
            }
        }

        //callbackContext.success(message);
        /*} else {
            callbackContext.error("Expected one non-empty string argument.");
        }*/
    }
}