package ca.mohawk.google_maps;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadURL {

    public String readURL(String myurl) throws IOException{
        String data = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;

        try{
            URL url = new URL(myurl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();

            inputStream = httpURLConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer ab = new StringBuffer();

            String line = "";
            while((line = br.readLine()) != null){
                ab.append(line);
            }

            data = ab.toString();
            br.close();

        } catch (MalformedURLException e){
            Log.i("DownloadUrl","readUrl: " + e.getMessage());
            e.printStackTrace();
        }
        catch(IOException e){
            e.printStackTrace();
        }
        finally{
            inputStream.close();
            httpURLConnection.disconnect();
        }

        Log.d("DownloadURL","Returning data= "+data);

        return data;
    }
}
