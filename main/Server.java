package org.nopad.pandorabox;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Server {
    String dataPath = null;
    boolean isRoot = false;
    Context mContext;
    String port = "8089";
    String sdCardPath = null;
    String serverPath = null;
    String version = "1.2.0";
    String wwwroot = "/data/data/com.ayansoft.androphp/lighttpd";

    public Server(Context c, String port, String wwwroot, boolean isroot) {
        this.mContext = c;
        this.port = port;
        this.wwwroot = wwwroot;
        this.isRoot = isroot;
        this.sdCardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        this.serverPath = "/data/data/com.ayansoft.androphp";
    }

    public void Start() {
        reloadServer();
    }

    public void checkVersion() {
        boolean bool = false;
        File f = new File(this.serverPath + "/version");
        String text = readTextFile(this.serverPath + "/version");
        if (f.exists()) {
            if (text == null) {
                bool = true;
            }
            if (!text.trim().equals(this.version)) {
                bool = true;
            }
        } else {
            bool = true;
        }
        if (bool) {
            deleteFolder(this.serverPath + "/instruction");
            deleteFolder(this.serverPath + "/php");
            deleteFolder(this.serverPath + "/mysql");
            deleteFolder(this.serverPath + "/lighttpd");
            deleteFolder(this.serverPath + "/lib");
            deleteFolder(this.serverPath + "/binary");
            extractZipFile();
            copyFileFromAssets("conf/version", this.serverPath + "/version");
        }
    }

    public boolean checkPort(int port) {
        Socket socket;
        try {
            Socket socket2 = new Socket("127.0.0.1", port);
            try {
                new PrintWriter(socket2.getOutputStream(), true).println("test");
                try {
                    socket2.close();
                    socket = socket2;
                    return true;
                } catch (IOException e) {
                    Log.v("server check port", e.getMessage());
                    socket = socket2;
                    return false;
                }
            } catch (IOException e2) {
                Log.v("server check port", e2.getMessage());
                socket = socket2;
                return false;
            }
        } catch (UnknownHostException e3) {
            Log.v("server check port", e3.getMessage());
            return false;
        } catch (IOException e22) {
            Log.v("server check port", e22.getMessage());
            return false;
        }
    }

    public void extractZipFile() {
        new Decompress(this.mContext, "files.zip", this.serverPath + "/").unzip();
    }

    public void setupConfigs() {
        File f = new File(this.wwwroot);
        if (!f.exists()) {
            f.mkdir();
        }
        String text = "";
        saveTextFile(this.serverPath + "/lighttpd/lighttpd.conf", readTextFileFromAssets("conf/lighttpd.conf").replace("${wwwroot}", this.wwwroot).replace("${port}", this.port).replace("${serverpath}", this.serverPath).replace("${logpath}", this.serverPath + "/lighttpd/tmp"));
        text = "";
        saveTextFile(this.serverPath + "/mysql/my.cnf", readTextFileFromAssets("conf/my.cnf").replace("${serverpath}", this.serverPath));
        text = "";
        saveTextFile(this.serverPath + "/php/php.ini", readTextFileFromAssets("conf/php.ini").replace("${serverpath}", this.serverPath));
        saveTextFile(this.wwwroot + "/phpinfo.php", "<?php \n   echo phpinfo();  \n?>");
        if (!f.exists()) {
            f.mkdir();
            try {
                FileUtils.chmod(f, 509);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setPermissions() {
        Exception e;
        try {
            File f = new File(this.serverPath + "/lighttpd/lighttpd");
            try {
                FileUtils.chmod(f, 511);
                FileUtils.chmod(new File(this.serverPath + "/lighttpd/killall"), 511);
                f = new File(this.serverPath + "/mysql/mysqld");
                FileUtils.chmod(f, 511);
                FileUtils.chmod(new File(this.serverPath + "/php/php"), 511);
                f = new File(this.serverPath + "/mysql/data");
                FileUtils.chmod(f.getParentFile(), 511);
                FileUtils.chmod(new File(this.serverPath + "/mysql/data/mysql").getParentFile(), 511);
            } catch (Exception e2) {
                e = e2;
                File file = f;
                e.printStackTrace();
            }
        } catch (Exception e3) {
            e = e3;
        }
    }

    public void reloadServer() {
        checkVersion();
        stopServer();
        setupConfigs();
        setPermissions();
        String lighttpdconf = "-D -f " + this.serverPath + "/lighttpd/lighttpd.conf";
        String mycnf = "--defaults-file=" + this.serverPath + "/mysql/my.cnf";
        String phpstr = new StringBuilder(String.valueOf(String.valueOf(this.serverPath + "/php/php -a -b 127.0.0.1:9009"))).append(" ").append("-c " + this.serverPath + "/php/php.ini").toString();
        String mystr = new StringBuilder(String.valueOf(String.valueOf(this.serverPath + "/mysql/mysqld"))).append(" ").append(mycnf).toString();
        String lighttpdstr = new StringBuilder(String.valueOf(String.valueOf(this.serverPath + "/lighttpd/lighttpd"))).append(" ").append(lighttpdconf).toString();
        execCommand(phpstr);
        execCommand(lighttpdstr);
        execCommand(mystr);
        if (this.isRoot) {
            runSuCommand(phpstr);
            runSuCommand(lighttpdstr);
            runSuCommand(mystr);
            return;
        }
        runCommand(phpstr);
        runCommand(lighttpdstr);
        runCommand(mystr);
    }

    public boolean stopServer() {
        Exception e;
        try {
            File f = new File(this.serverPath + "/lighttpd/killall");
            File file;
            try {
                FileUtils.chmod(f, 511);
                file = f;
            } catch (Exception e2) {
                e = e2;
                file = f;
                e.printStackTrace();
                execCommand(this.serverPath + "/lighttpd/killall lighttpd");
                runCommand(this.serverPath + "/lighttpd/killall lighttpd");
                execCommand(this.serverPath + "/lighttpd/killall mysqld");
                runCommand(this.serverPath + "/lighttpd/killall mysqld");
                execCommand(this.serverPath + "/lighttpd/killall php/php");
                runCommand(this.serverPath + "/lighttpd/killall php");
                return true;
            }
        } catch (Exception e3) {
            e = e3;
            e.printStackTrace();
            execCommand(this.serverPath + "/lighttpd/killall lighttpd");
            runCommand(this.serverPath + "/lighttpd/killall lighttpd");
            execCommand(this.serverPath + "/lighttpd/killall mysqld");
            runCommand(this.serverPath + "/lighttpd/killall mysqld");
            execCommand(this.serverPath + "/lighttpd/killall php/php");
            runCommand(this.serverPath + "/lighttpd/killall php");
            return true;
        }
        execCommand(this.serverPath + "/lighttpd/killall lighttpd");
        runCommand(this.serverPath + "/lighttpd/killall lighttpd");
        execCommand(this.serverPath + "/lighttpd/killall mysqld");
        runCommand(this.serverPath + "/lighttpd/killall mysqld");
        execCommand(this.serverPath + "/lighttpd/killall php/php");
        runCommand(this.serverPath + "/lighttpd/killall php");
        return true;
    }

    public static void runCommand(String command) {
        try {
            OutputStream os = Runtime.getRuntime().exec("sh").getOutputStream();
            Log.d("", "runCommand() cmd=" + command);
            writeLine(os, command);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void execCommand(String str) {
        try {
            Runtime.getRuntime().exec(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void runSuCommand(String sucommand) {
        try {
            OutputStream os = Runtime.getRuntime().exec("su -c sh").getOutputStream();
            Log.d("", "runSuCommand() cmd=" + sucommand);
            writeLine(os, sucommand);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeLine(OutputStream os, String value) throws IOException {
        os.write(new StringBuilder(String.valueOf(value)).append("\n").toString().getBytes());
    }

    public boolean copyFileFromAssets(String fileName, String outFileName) {
        Exception e;
        OutputStream outputStream;
        try {
            InputStream in = this.mContext.getAssets().open(fileName);
            OutputStream out = new FileOutputStream(outFileName);
            try {
                byte[] buffer = new byte[1024];
                while (true) {
                    int read = in.read(buffer);
                    if (read == -1) {
                        try {
                            in.close();
                            out.flush();
                            out.close();
                            return true;
                        } catch (Exception e2) {
                            e = e2;
                            outputStream = out;
                            e.printStackTrace();
                            return false;
                        }
                    }
                    out.write(buffer, 0, read);
                }
            } catch (IOException e3) {
                e3.printStackTrace();
                outputStream = out;
                return false;
            }
        } catch (Exception e4) {
            e = e4;
            e.printStackTrace();
            return false;
        }
    }

    public boolean copyFile(File src, File dst) {
        try {
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dst);
            byte[] buf = new byte[1024];
            while (true) {
                int len = in.read(buf);
                if (len <= 0) {
                    in.close();
                    out.close();
                    return true;
                }
                out.write(buf, 0, len);
            }
        } catch (IOException e) {
            return false;
        }
    }

    public boolean saveTextFile(String fileName, String text) {
        try {
            FileWriter fWriter = new FileWriter(fileName);
            fWriter.write(text);
            fWriter.flush();
            fWriter.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String readTextFileFromAssets(String fileName) {
        try {
            InputStream stream = this.mContext.getAssets().open(fileName);
            byte[] buffer = new byte[stream.available()];
            stream.read(buffer);
            stream.close();
            String text = new String(buffer);
            return text;
        } catch (IOException e) {
            return null;
        }
    }

    public String readTextFile(String fileName) {
        File file = new File(fileName);
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    return text.toString();
                }
                text.append(line);
                text.append('\n');
            }
        } catch (IOException e) {
            return null;
        }
    }

    public boolean deleteFolder(String path) {
        File dir = new File(path);
        Log.d("DeleteRecursive", "DELETEPREVIOUS TOP" + dir.getPath());
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String file : children) {
                File temp = new File(dir, file);
                if (temp.isDirectory()) {
                    Log.d("DeleteRecursive", "Recursive Call" + temp.getPath());
                    deleteFolder(temp.getPath());
                } else {
                    Log.d("DeleteRecursive", "Delete File" + temp.getPath());
                    if (!temp.delete()) {
                        Log.d("DeleteRecursive", "DELETE FAIL");
                        return false;
                    }
                }
            }
            dir.delete();
        }
        return true;
    }
}
