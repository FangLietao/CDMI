/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.snia.cdmiserver.dacHttp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minidev.json.parser.ParseException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

/**
 *
 * @author 310253695
 */
public class DacRequest {

    String path = "http://192.168.199.161:8080/DAC/rest/DACContainer/Test2.txt";

    public enum method {

    }

    public void setPath(String path) {
        this.path = path;
    }

    public DacResponseEntity operation(Request.Method method, String entity) throws URISyntaxException, IOException {
        Request req = new Request(method, path);

        HttpResponse response = req.withContentType("application/dac-object").withAuthorization("Wang").withEntity(entity).send();

        HttpEntity httpEntity = response.getEntity();
        InputStream instreams = httpEntity.getContent();
        InputStreamReader r = new InputStreamReader(instreams);
        BufferedReader buffer = new BufferedReader(r);

        StringBuilder s = new StringBuilder();
        String line;
        while ((line = buffer.readLine()) != null) {
            s.append(line);
        }

        DacResponseEntity resEntity = new DacResponseEntity();
        try {
            resEntity.fromJSONString(s.toString());
        } catch (ParseException ex) {
            Logger.getLogger(DacRequest.class.getName()).log(Level.SEVERE, null, ex);
        }

        return resEntity;

    }

}
