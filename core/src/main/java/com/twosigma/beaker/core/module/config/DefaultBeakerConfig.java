/*
 *  Copyright 2014 TWO SIGMA INVESTMENTS, LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.twosigma.beaker.core.module.config;

import com.google.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * DefaultBeakerConfig
 * holds the default beaker configuration which used by DefaultBeakerConfigModule who binds it
 * to BeakerConfig interface. The configuration is determined with both constant default
 * values and the input of BeakerConfigPref
 */
public class DefaultBeakerConfig implements BeakerConfig {

  private final String installDir;
  private final String pluginDir;
  private final String dotDir;
  private final String nginxDir;
  private final String nginxBinDir;
  private final String nginxServDir;
  private final String nginxExtraRules;
  private final Boolean useKerberos;
  private final Integer portBase;
  private final String defaultNotebookUrl;
  private final Map<String, String> pluginOptions;
  private final Map<String, String[]> pluginEnvps;

  @Inject
  public DefaultBeakerConfig(BeakerConfigPref pref) throws UnknownHostException {

    this.installDir = System.getProperty("user.dir");
    this.useKerberos = pref.getUseKerberos();
    this.portBase = pref.getPortBase();
    this.pluginDir = this.installDir + "/src/main/sh";
    this.dotDir = System.getProperty("user.home") + "/.beaker";
    File dotFile = new File(dotDir);
    if (!dotFile.exists()) {
      if (!dotFile.mkdir()) {
        System.out.println("failed to create " + dotDir);
      }
    }
    this.nginxDir = this.installDir + "/nginx";
    this.nginxBinDir = this.installDir + "/nginx/bin";
    this.nginxServDir = this.dotDir + "/nginx";
    this.nginxExtraRules = "";


    final String prefDefaultNotebookUrl = pref.getDefaultNotebookUrl();
    final String mainDefaultNotebookPath = this.dotDir + "/default.bkr";
    final String defaultDefaultNotebookPath = this.installDir + "/config/default.bkr";

    if (prefDefaultNotebookUrl != null) {
      this.defaultNotebookUrl = prefDefaultNotebookUrl;
    } else {
      ensureMainDefaultNotebook(
          new File(mainDefaultNotebookPath),
          new File(defaultDefaultNotebookPath));
      this.defaultNotebookUrl = mainDefaultNotebookPath;
    }
    this.pluginOptions = pref.getPluginOptions();
    this.pluginEnvps = new HashMap<>();
  }

  private static void ensureMainDefaultNotebook(
      File mainDefaultNotebook,
      File defaultDefaultNotebook) {

    if (readFile(mainDefaultNotebook) == null) {
      // main default notebook has no content,
      // try copying from the default defaultNotebook.
      if (readFile(defaultDefaultNotebook) != null) {
        try {
          copyFile(defaultDefaultNotebook, mainDefaultNotebook);
        } catch (FileNotFoundException e) {
          System.out.println("ERROR writing main default notebook, " + e);
        }
      } else {
        // the default defaultNotebook also has no content?!
        System.out.println("Double bogey, default notebook is empty.");
      }
    }
  }

  private static void copyFile(File from, File to) throws FileNotFoundException {
    PrintWriter out = new PrintWriter(to);
    out.print(readFile(from));
    out.close();
  }

  @Override
  public String getInstallDirectory() {
    return this.installDir;
  }

  @Override
  public String getPluginDirectory() {
    return this.pluginDir;
  }

  @Override
  public String getDotDirectory() {
    return this.dotDir;
  }

  @Override
  public String getNginxDirectory() {
    return this.nginxDir;
  }

  @Override
  public String getNginxBinDirectory() {
    return this.nginxBinDir;
  }

  @Override
  public String getNginxServDirectory() {
    return this.nginxServDir;
  }

  @Override
  public String getNginxExtraRules() {
    return this.nginxExtraRules;
  }

  @Override
  public Integer getPortBase() {
    return this.portBase;
  }

  @Override
  public Boolean getUseKerberos() {
    return this.useKerberos;
  }

  @Override
  public String getDefaultNotebookUrl() {
    return this.defaultNotebookUrl;
  }

  static private String readFile(File file) {
    try {
      FileInputStream fis = new FileInputStream(file);
      byte[] data = new byte[(int) file.length()];
      fis.read(data);
      fis.close();
      return new String(data, "UTF-8");
    } catch (FileNotFoundException e) {
      System.out.println("ERROR reading file" + file.getName() + ": " + e);
    } catch (IOException e) {
      System.out.println("ERROR reading file" + file.getName() + ": " + e);
    }
    return null;
  }

  @Override
  public Map<String, String> getPluginOptions() {
    return this.pluginOptions;
  }

  @Override
  public Map<String, String[]> getPluginEnvps() {
    return this.pluginEnvps;
  }

}
