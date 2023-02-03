package br.com.yamarasolution.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONObject;

public class ReadJsonFileToJsonObject {

  /**
   * It reads the contents of a file and returns a JSONObject
   * 
   * @return A JSONObject
   */
  public JSONObject read() throws IOException {
    String file = "src/main/resources/openapi/response.json";
    String content = new String(Files.readAllBytes(Paths.get(file)));
    return new JSONObject(content);
  }

}
