package com.first.translate;

import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.JBColor;
import org.apache.http.HttpEntity;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class translate extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        //Gets the current project entity
        Project project = e.getData(PlatformDataKeys.PROJECT);
        //Gets the current mouse
        final Editor mEditor = e.getData(PlatformDataKeys.EDITOR);
        if (null == mEditor) {
            return;
        }
        //Gets the selected mouse
        SelectionModel model = mEditor.getSelectionModel();
        //text
        final String selectedText = model.getSelectedText();
        if (TextUtils.isEmpty(selectedText)) {
            return;
        }
        showPopupBalloon(mEditor,doGet("http://fanyi.youdao.com/translate?&doctype=json&type=AUTO&i="+selectedText));
        //http://fanyi.youdao.com/translate?&doctype=json&type=AUTO&i=你好
    }
    //Display popup window
    private void showPopupBalloon(final Editor editor, final String result) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                JBPopupFactory factory = JBPopupFactory.getInstance();
                factory.createHtmlTextBalloonBuilder(result, null, new JBColor(new Color(186, 238, 186), new Color(73, 117, 73)), null)
                        .setFadeoutTime(5000)
                        .createBalloon()
                        .show(factory.guessBestPopupLocation(editor), Balloon.Position.below);
            }
        });
    }



    public static String doGet(String url) {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        String result = "";
        try {
            // Create an instance of httpClient with the address default configuration
            httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(url);
//            httpGet.setHeader("Authorization", "Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0");
            // Connection host service timeout
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(35000)
                    // Request timeout
                    .setConnectionRequestTimeout(35000)
                    // Data read timeout
                    .setSocketTimeout(60000)
                    .build();
            httpGet.setConfig(requestConfig);
            response = httpClient.execute(httpGet);
            // Gets the returned data by returning the object
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity);
            List<List<JSONObject>> re = (List<List<JSONObject>>)JSONObject.parseObject(result, HashMap.class).get("translateResult");
            result= ""+re.get(0).get(0).get("tgt");
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != response) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
}
