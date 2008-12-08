package org.objectweb.proactive.core.debug.dconnection;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;


public class DebuggeePortSetter {

    public static void main(String[] argv) throws Exception {
        System.out.println(Arrays.toString(argv));
        String dir = System.getProperty("java.io.tmpdir");
        String name = argv[0];
        String port = argv[2];

        File file = new File(dir + File.separator + name);
        if (file.isFile()) {
            file.delete();
        }
        file.createNewFile();
        FileWriter writer = new FileWriter(file);
        writer.write(port);
        writer.close();
    }

}
