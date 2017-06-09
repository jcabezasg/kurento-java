/*
 * (C) Copyright 2013 Kurento (http://kurento.org/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.kurento.repository.internal.repoimpl.filesystem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public class ItemsMetadata {

  private final Logger log = LoggerFactory.getLogger(ItemsMetadata.class);

  // TODO Avoid potential memory leaks using Google's MapMaker
  private ConcurrentMap<String, Map<String, String>> itemsMetadata;

  private final File itemsMetadataFile;

  public ItemsMetadata(File itemsMetadataFile) {
    this.itemsMetadataFile = itemsMetadataFile;
    try {
      loadItemsMetadata();
    } catch (IOException e) {
      log.warn("Exception while loading items metadata", e);
    }
  }

  private void loadItemsMetadata() throws IOException {
    itemsMetadata = new ConcurrentHashMap<>();
    DBObject contents = (DBObject) JSON.parse(loadFileAsString());
    if (contents != null) {
      for (String key : contents.keySet()) {
        try {
          DBObject metadata = (DBObject) contents.get(key);
          Map<String, String> map = new HashMap<>();
          for (String metadataKey : metadata.keySet()) {
            map.put(metadataKey, metadata.get(metadataKey).toString());
          }
          itemsMetadata.put(key, map);
        } catch (ClassCastException e) {
          log.warn("Attribute '{}' should be an object", key);
        }
      }
    }
  }

  private String loadFileAsString() throws IOException {

    if (!itemsMetadataFile.exists()) {
      return "";
    }

    StringBuilder sb = new StringBuilder();
    try (FileReader metadataFile = new FileReader(itemsMetadataFile)) {
      try (BufferedReader br = new BufferedReader(metadataFile)) {
        String line;
        while ((line = br.readLine()) != null) {
          sb.append(line).append("\n");
        }
      }
    }
    return sb.toString();
  }

  public synchronized void setMetadataForId(String id, Map<String, String> metadata) {
    itemsMetadata.put(id, metadata);
  }

  public synchronized Map<String, String> loadMetadata(String id) {
    Map<String, String> metadata = itemsMetadata.get(id);
    if (metadata == null) {
      metadata = new HashMap<>();
      itemsMetadata.put(id, metadata);
    }
    return metadata;
  }

  public List<Entry<String, Map<String, String>>> findByAttValue(String attributeName,
      String value) {

    List<Entry<String, Map<String, String>>> list = new ArrayList<>();

    for (Entry<String, Map<String, String>> item : itemsMetadata.entrySet()) {
      String attValue = item.getValue().get(attributeName);
      if (attValue != null && attValue.equals(value)) {
        list.add(item);
      }
    }

    return list;
  }

  public List<Entry<String, Map<String, String>>> findByAttRegex(String attributeName,
      String regex) {

    Pattern pattern = Pattern.compile(regex);

    List<Entry<String, Map<String, String>>> list = new ArrayList<>();

    for (Entry<String, Map<String, String>> item : itemsMetadata.entrySet()) {
      String value = item.getValue().get(attributeName);
      if (value != null && pattern.matcher(value).matches()) {
        list.add(item);
      }
    }

    return list;
  }

  public void save() {

    try {
      if (!itemsMetadataFile.exists()) {
        itemsMetadataFile.getParentFile().mkdirs();
        itemsMetadataFile.createNewFile();
      }
      try (PrintWriter writer = new PrintWriter(itemsMetadataFile)) {
        String content = JSON.serialize(itemsMetadata);
        writer.print(content);
      }
    } catch (IOException e) {
      log.error("Exception writing metadata file", e);
    }
  }
}
