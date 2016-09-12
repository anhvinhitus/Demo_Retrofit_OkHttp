package vn.com.vng.zalopay.data.appresources;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import timber.log.Timber;

/**
 * Created by AnhHieu on 5/21/16.
 */
public class FileUtil {

    private static final int WRITE_BUFFER_SIZE = 1024 * 8;

    public static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[4096];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    public static void unzip(String zipFilePath, String destinationPath) throws Exception {

            File archive = new File(zipFilePath);

            ZipFile zipfile = new ZipFile(archive);

            for (Enumeration e = zipfile.entries(); e.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) e.nextElement();

                Timber.d("Unzip file %s", entry.getName());
                unzipEntry(zipfile, entry, destinationPath);

            }

            zipfile.close();

    }

    public static void decompress(byte[] compressed, String location) throws IOException {
        try {
            File destinationFolder = new File(location);
            destinationFolder.mkdirs();
        } catch (Exception e) {
            Timber.e(e, "Error while creating destination folder for holding decompressed contents");
        }

        InputStream is;
        ZipInputStream zis;

        String filename;
        is = new ByteArrayInputStream(compressed);
        zis = new ZipInputStream(new BufferedInputStream(is));
        ZipEntry ze;
        byte[] buffer = new byte[1024];
        int count;

        while ((ze = zis.getNextEntry()) != null)
        {
            filename = ze.getName();

            // Need to create directories if not exists, or
            // it will generate an Exception...
            if (ze.isDirectory()) {
                String path = location + File.separator + filename;
                File fmd = new File(path);
                fmd.mkdirs();

                hideImageFromGallery(path + File.separator);

                continue;
            }

            FileOutputStream fout = new FileOutputStream(location + File.separator + filename);

            while ((count = zis.read(buffer)) != -1) {
                fout.write(buffer, 0, count);
            }

            fout.close();
            zis.closeEntry();
        }

        zis.close();

    }


    /**
     * Create .nomedia file in order to prevent gallery application shows this
     * folder into album
     *
     * @param path
     *            Local path
     *
     * @throws IOException
     *             if it's not possible to create the file.
     */
    public static void hideImageFromGallery(String path) throws IOException {
        String NOMEDIA = ".nomedia";
        File nomediaFile = new File(path + NOMEDIA);
        if (!nomediaFile.exists()) {
            nomediaFile.createNewFile();
        }
    }

    public static void unzipEntry(ZipFile zipfile, ZipEntry entry, String outputDir) throws IOException {

        if (entry.isDirectory()) {
            File dir = new File(outputDir, entry.getName());
            createDir(dir);
            hideImageFromGallery(dir.getPath() + File.separator);
            return;
        }

        File outputFile = new File(outputDir, entry.getName());
        if (!outputFile.getParentFile().exists()) {
            createDir(outputFile.getParentFile());
        }

        Timber.v("Extracting: " + entry);

        InputStream zin = zipfile.getInputStream(entry);
        BufferedInputStream inputStream = new BufferedInputStream(zin);
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

        try {
            copyFile(inputStream, outputStream);
        } finally {
            outputStream.close();
            inputStream.close();
        }
    }

    public static void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }

        directory.delete();
    }

    public static void deleteFile(String path) {
        try {
            File oldDir = new File(path);
            deleteRecursive(oldDir);
        } catch (Exception ex) {
        }
    }

    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        fileOrDirectory.delete();
    }
    public static void unzipFile(File zipFile, String destination, boolean deleteExisting) throws IOException {
        FileInputStream fileStream = null;
        BufferedInputStream bufferedStream = null;
        ZipInputStream zipStream = null;
        try {
            fileStream = new FileInputStream(zipFile);
            bufferedStream = new BufferedInputStream(fileStream);
            zipStream = new ZipInputStream(bufferedStream);
            ZipEntry entry;

            File destinationFolder = new File(destination);
            if (deleteExisting && destinationFolder.exists()) {
                deleteDirectory(destinationFolder);
            }

            destinationFolder.mkdirs();

            byte[] buffer = new byte[WRITE_BUFFER_SIZE];
            while ((entry = zipStream.getNextEntry()) != null) {
                String fileName = entry.getName();
                File file = new File(destinationFolder, fileName);
                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    File parent = file.getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }

                    FileOutputStream fout = new FileOutputStream(file);
                    try {
                        int numBytesRead;
                        while ((numBytesRead = zipStream.read(buffer)) != -1) {
                            fout.write(buffer, 0, numBytesRead);
                        }
                    } finally {
                        fout.close();
                    }
                }
                long time = entry.getTime();
                if (time > 0) {
                    file.setLastModified(time);
                }
            }
        } finally {
            try {
                if (zipStream != null) zipStream.close();
                if (bufferedStream != null) bufferedStream.close();
                if (fileStream != null) fileStream.close();
            } catch (IOException e) {
                throw new IOException("Error closing IO resources.", e);
            }
        }
    }

    public static void createDir(File dir) {

        if (dir.exists()) {
            return;
        }

        Timber.v("Creating dir " + dir.getName());

        if (!dir.mkdirs()) {

            throw new RuntimeException("Can not create dir " + dir);
        }
    }

    static File ensureDirectory(String path) {
        File file = new File(path);
        ensureDirectory(file);
        return file;
    }

    static void ensureDirectory(File dir) {
        dir.mkdirs();
    }
}
