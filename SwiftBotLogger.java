import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SwiftBotLogger {
    private BufferedWriter writer;
    private StringBuilder shapeLog;
    private long totalTime;
    private int shapeCount;
    private Map<String, Integer> shapeFrequency;
    private double largestShapeArea;
    private String largestShape;

    public SwiftBotLogger(String fileName) {
        try {
            // Initialize the writer to write to the specified file.
            File logFile = new File(fileName);
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            writer = new BufferedWriter(new FileWriter(logFile, true));

            // Initialize tracking variables
            shapeLog = new StringBuilder();
            totalTime = 0;
            shapeCount = 0;
            shapeFrequency = new HashMap<>();
            largestShapeArea = 0;
            largestShape = "";
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1); // Ensure the program exits if the log file cannot be opened
        }
    }

    // Logs the shape (Square) and its size and time taken
    public synchronized void logShape(String shapeName, int size, long timeTaken) {
        // Calculate the area of the shape (Square area = side^2)
        double area = size * size;

        // Add to shape log
        shapeLog.append(shapeName).append(": ").append(size)
                .append(" (time: ").append(timeTaken / 1000.0).append(" seconds), ");

        // Track total time and shape count
        totalTime += timeTaken;
        shapeCount++;

        // Track shape frequency
        shapeFrequency.put(shapeName, shapeFrequency.getOrDefault(shapeName, 0) + 1);

        // Track the largest shape by area
        if (area > largestShapeArea) {
            largestShapeArea = area;
            largestShape = shapeName + ": " + size;
        }
    }

    // Logs the shape (Triangle) and its sides, angles, and time taken
    public synchronized void logShape(String shapeName, int sideA, int sideB, int sideC, double angleA, double angleB, double angleC, long timeTaken) {
        // Calculate the area of the triangle using Heron's formula
        double semiPerimeter = (sideA + sideB + sideC) / 2.0;
        double area = Math.sqrt(semiPerimeter * (semiPerimeter - sideA) * (semiPerimeter - sideB) * (semiPerimeter - sideC));

        // Add to shape log
        shapeLog.append(shapeName).append(": ").append(sideA).append(", ").append(sideB).append(", ").append(sideC)
                .append(" (angles: ")
                .append(String.format("%.2f", angleA)).append(", ")
                .append(String.format("%.2f", angleB)).append(", ")
                .append(String.format("%.2f", angleC)).append("; time: ")
                .append(timeTaken / 1000.0).append(" seconds), ");

        // Track total time and shape count
        totalTime += timeTaken;
        shapeCount++;

        // Track shape frequency
        shapeFrequency.put(shapeName, shapeFrequency.getOrDefault(shapeName, 0) + 1);

        // Track the largest shape by area
        if (area > largestShapeArea) {
            largestShapeArea = area;
            largestShape = shapeName + ": " + sideA;  // Just use one side as an identifier
        }
    }

    // Logs the shape (Pentagon or Hexagon) and its size and time taken
    public synchronized void logPolygonShape(String shapeName, int sideLength, long timeTaken) {
        // Calculate the area of the polygon
        double area = 0;
        if (shapeName.equals("Pentagon")) {
            area = (1.0 / 4.0) * Math.sqrt(5 * (5 + 2 * Math.sqrt(5))) * Math.pow(sideLength, 2);
        } else if (shapeName.equals("Hexagon")) {
            area = (3 * Math.sqrt(3) / 2.0) * Math.pow(sideLength, 2);
        }

        // Add to shape log
        shapeLog.append(shapeName).append(": ").append(sideLength)
                .append(" (time: ").append(timeTaken / 1000.0).append(" seconds), ");

        // Track total time and shape count
        totalTime += timeTaken;
        shapeCount++;

        // Track shape frequency
        shapeFrequency.put(shapeName, shapeFrequency.getOrDefault(shapeName, 0) + 1);

        // Track the largest shape by area
        if (area > largestShapeArea) {
            largestShapeArea = area;
            largestShape = shapeName + ": " + sideLength;
        }
    }

    // Finalizes the log file and writes the additional summary info
    public void finalizeLog() {
        try {
            if (writer != null) {
                // Write the shape log
                writer.write("Shapes drawn: ");
                writer.write(shapeLog.toString().replaceAll(", $", ""));  // Remove trailing comma
                writer.newLine();

                // Log the largest shape drawn
                writer.write("Largest shape: " + largestShape);
                writer.newLine();

                // Log the most frequent shape
                String mostFrequentShape = shapeFrequency.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(entry -> entry.getKey() + ": " + entry.getValue() + " times")
                        .orElse("No shapes drawn");
                writer.write("Most frequent shape: " + mostFrequentShape);
                writer.newLine();

                // Log the average time taken
                if (shapeCount > 0) {
                    double averageTime = (double) totalTime / shapeCount;
                    writer.write("Average time: " + String.format("%.2f", averageTime / 1000.0) + " seconds");
                    writer.newLine();
                } else {
                    writer.write("No shapes drawn.");
                    writer.newLine();
                }

                // Finalize the log
                writer.close();
                System.out.println("Data has been successfully saved to the log file.");
                System.out.println("/data/home/pi/shapes_log.txt");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
