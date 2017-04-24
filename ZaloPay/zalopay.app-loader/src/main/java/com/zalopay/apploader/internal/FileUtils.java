package com.zalopay.apploader.internal;


import android.content.Context;
import android.support.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import timber.log.Timber;

/**
 * Created by huuhoa on 4/29/16.
 * Copy from react-native-code-push
 */
public class FileUtils {
    private static final int WRITE_BUFFER_SIZE = 1024 * 8;

    public static void copyDirectoryContents(String sourceDirectoryPath, String destinationDirectoryPath)
            throws MiniApplicationException, IOException {
        File sourceDir = new File(sourceDirectoryPath);
        File destDir = new File(destinationDirectoryPath);
        if (!destDir.exists()) {
            destDir.mkdir();
        }

        for (File sourceFile : sourceDir.listFiles()) {
            if (sourceFile.isDirectory()) {
                copyDirectoryContents(
                        MdlUtils.appendPathComponent(sourceDirectoryPath, sourceFile.getName()),
                        MdlUtils.appendPathComponent(destinationDirectoryPath, sourceFile.getName()));
            } else {
                File destFile = new File(destDir, sourceFile.getName());
                FileInputStream fromFileStream = null;
                BufferedInputStream fromBufferedStream = null;
                FileOutputStream destStream = null;
                byte[] buffer = new byte[WRITE_BUFFER_SIZE];
                try {
                    fromFileStream = new FileInputStream(sourceFile);
                    fromBufferedStream = new BufferedInputStream(fromFileStream);
                    destStream = new FileOutputStream(destFile);
                    int bytesRead;
                    while ((bytesRead = fromBufferedStream.read(buffer)) > 0) {
                        destStream.write(buffer, 0, bytesRead);
                    }
                } finally {
                    try {
                        if (fromFileStream != null) fromFileStream.close();
                        if (fromBufferedStream != null) fromBufferedStream.close();
                        if (destStream != null) destStream.close();
                    } catch (IOException e) {
                        throw new MiniApplicationException("Error closing IO resources.", e);
                    }
                }
            }
        }
    }

    public static void deleteDirectory(File directory, boolean deleteParent) {
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

        if (deleteParent) {
            directory.delete();
        }
    }

    public static void deleteDirectory(File directory) {
        deleteDirectory(directory, true);
    }

    public static void deleteDirectoryAtPath(String directoryPath) {
        deleteDirectory(new File(directoryPath));
    }

    public static void deleteFileAtPathSilently(String path) {
        deleteFileOrFolderSilently(new File(path));
    }

    public static void deleteFileOrFolderSilently(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File fileEntry : files) {
                if (fileEntry.isDirectory()) {
                    deleteFileOrFolderSilently(fileEntry);
                } else {
                    if (!file.delete()) {
                        fileEntry.delete();
                    }
                }
            }
        }

        if (!file.delete()) {
            MdlUtils.log("Error deleting file " + file.getName());
        }
    }

    public static boolean fileAtPathExists(String filePath) {
        return new File(filePath).exists();
    }

    public static void moveFile(File fileToMove, String newFolderPath, String newFileName) throws MiniApplicationException {
        File newFolder = new File(newFolderPath);
        if (!newFolder.exists()) {
            newFolder.mkdirs();
        }

        File newFilePath = new File(newFolderPath, newFileName);
        if (!fileToMove.renameTo(newFilePath)) {
            throw new MiniApplicationException("Unable to move file from " +
                    fileToMove.getAbsolutePath() + " to " + newFilePath.getAbsolutePath() + ".");
        }
    }

    public static String readFileToString(String filePath) throws IOException {
        FileInputStream fin = null;
        BufferedReader reader = null;
        try {
            File fl = new File(filePath);
            fin = new FileInputStream(fl);
            reader = new BufferedReader(new InputStreamReader(fin));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }

            return sb.toString();
        } finally {
            if (reader != null) reader.close();
            if (fin != null) fin.close();
        }
    }

    public static void writeStringToFile(String content, String filePath) throws IOException {
        PrintWriter out = null;
        try {
            out = new PrintWriter(filePath);
            out.print(content);
        } finally {
            if (out != null) out.close();
        }
    }

    @Nullable
    public static String writeByteArrayToFile(byte[] byteArray, String filePath) {
        File file = new File(filePath);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(byteArray);
            return file.getAbsolutePath();
        } catch (IOException io) {
            Timber.d(io, "write png byteArray to file: ");
        } finally {
            if (fos != null) {
                try {
                    fos.flush();
                    fos.close();
                } catch (IOException ignore) {
                }
            }
        }
        return null;
    }

    public static void unzipFile(InputStream stream, String destination, boolean deleteExisting) throws IOException, MiniApplicationException {
        BufferedInputStream bufferedStream = null;
        ZipInputStream zipStream = null;
        try {
            bufferedStream = new BufferedInputStream(stream);
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
            } catch (IOException e) {
                throw new MiniApplicationException("Error closing IO resources.", e);
            }
        }
    }

    public static void unzipFile(String zipFile, String destination, boolean deleteExisting) throws IOException, MiniApplicationException {
        FileInputStream fileStream = new FileInputStream(zipFile);

        unzipFile(fileStream, destination, deleteExisting);
    }

    public static String loadStringFromStream(InputStream is) {
        String json = null;
        try {
            int size = is.available();
            byte[] buffer = new byte[size];
            if (is.read(buffer) < 0) {
                buffer[0] = 0;
            }
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            Timber.e(ex, "loadStringFromStream exception [%s]", ex.getMessage());
            return null;
        }
        return json;
    }

    public static boolean mkdirs(File directory) {
        if (directory.exists()) {
            if (directory.isDirectory()) {
                return true;
            }
            directory.delete();
        }

        return !directory.mkdirs() && !directory.isDirectory();
    }

    @Nullable
    public static String writeStringToFile(Context context, String content, String fileName) {
        FileWriter out = null;
        try {
            File directory = new File(context.getCacheDir() + File.separator + "data");
            mkdirs(directory);
            File file = new File(directory, fileName);
            out = new FileWriter(file);
            out.write(content);
            return file.getAbsolutePath();
        } catch (IOException e) {
            Timber.d(e, "writeStringToFile: ");
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ignore) {
            }
        }
        return null;
    }

    public static void zip(String[] files, String zipFile) throws IOException {
        BufferedInputStream origin = null;
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
        try {
            byte data[] = new byte[WRITE_BUFFER_SIZE];

            for (int i = 0; i < files.length; i++) {
                FileInputStream fi = new FileInputStream(files[i]);
                origin = new BufferedInputStream(fi, WRITE_BUFFER_SIZE);
                try {
                    ZipEntry entry = new ZipEntry(files[i].substring(files[i].lastIndexOf("/") + 1));
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, WRITE_BUFFER_SIZE)) != -1) {
                        out.write(data, 0, count);
                    }
                } finally {
                    origin.close();
                }
            }
        } finally {
            out.close();
        }
    }

}
