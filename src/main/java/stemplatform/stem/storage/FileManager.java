package stemplatform.stem.storage;

import java.io.*;

public class FileManager {

    private static final String DATA_DIRECTORY = "data";
    private static final String DATA_FILE =
            DATA_DIRECTORY + File.separator + "application.dat";

    public FileManager() {
        createDataDirectory();
    }

    private void createDataDirectory() {
        File directory = new File(DATA_DIRECTORY);

        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    public void save(ApplicationState state) {

        try (ObjectOutputStream output =
                     new ObjectOutputStream(
                             new FileOutputStream(DATA_FILE))) {

            output.writeObject(state);

        } catch (IOException e) {
            System.out.println(
                    "Error saving application data: "
                            + e.getMessage()
            );
        }
    }

    public ApplicationState load() {

        File file = new File(DATA_FILE);

        if (!file.exists()) {
            return new ApplicationState();
        }

        try (ObjectInputStream input =
                     new ObjectInputStream(
                             new FileInputStream(file))) {

            return (ApplicationState) input.readObject();

        } catch (IOException | ClassNotFoundException e) {

            System.out.println(
                    "Error loading application data: "
                            + e.getMessage()
            );

            return new ApplicationState();
        }
    }
}