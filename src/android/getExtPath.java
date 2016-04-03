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
                    String encodedImage = "";
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
                        byte[] imageByte = Base64.decode(encodedImage, Base64.DEFAULT);
                        try {
                            String pathtoImg = Environment.getExternalStorageDirectory()+"/"+thisId+".jpg";
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
                        
                    }


                    items.put("Id", thisId);
                    items.put("Album", album);
                    items.put("Author", author);
                    items.put("Title", title);
                    items.put("Genre", genero);
                    items.put("Cover", encoded);
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
