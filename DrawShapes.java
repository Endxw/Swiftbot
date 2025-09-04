import swiftbot.*;
import java.awt.image.BufferedImage;

public class DrawShapes {
    static SwiftBotAPI swiftBot;
    static SwiftBotLogger logger;

    public static void main(String[] args) throws InterruptedException {
    	try {
            // Initialise SwiftBot API and logger
            swiftBot = new SwiftBotAPI();
            logger = new SwiftBotLogger("shapes_log.txt"); // Log file for storing shape-related logs
        } catch (Exception e) {
            // Handle the case where I2C is disabled and provide instructions to enable it
            System.out.println("\nI2C disabled!");
            System.out.println("Run the following command:");
            System.out.println("sudo raspi-config nonint do_i2c 0\n");
            System.exit(5); // Exit with an error code
        }

        mainMenu(); // Display the main menu

        // Keep the program running indefinitely
        while (true) {
            Thread.sleep(100);
        }
    }

    public static void mainMenu() throws InterruptedException {
        // Display the main menu options
        System.out.println("\n---------------------------------------------------------------------");
        System.out.println("\t\t\tMain Menu:");
        System.out.println("\t\t\tPress Button A to continue.");
        System.out.println("\t\t\tPress Button X to exit the program.");
        System.out.println("---------------------------------------------------------------------");

        // Disable buttons initially to prevent unintended inputs
        swiftBot.disableButton(Button.A);
        swiftBot.disableButton(Button.X);

        enableButtonInputs(); // Enable button inputs
    }


    public static void enableButtonInputs() throws InterruptedException {
        // Enable Button A to start scanning QR codes
        swiftBot.enableButton(Button.A, () -> {
            System.out.println("\n---------------------------------------------------------------------");
            System.out.println("");
            System.out.println("Button A pressed: Scanning QR Code for shapes...");
            System.out.println("");
            System.out.println("---------------------------------------------------------------------");
            scanQRCode(); // Start QR code scanning
        });

        // Enable Button X to exit the program
        swiftBot.enableButton(Button.X, () -> {
            System.out.println("\n---------------------------------------------------------------------");
            System.out.println("");
            System.out.println("Button X pressed: Exiting the program...");
            System.out.println("");
            System.out.println("---------------------------------------------------------------------");
            logger.finalizeLog(); // Finalise log before exiting
            System.exit(0); // Exit program
        });
    }

    public static void scanQRCode() {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + 10000; // 10-second time limit for scanning
        String decodedMessage = "";

        while (System.currentTimeMillis() < endTime) {
            BufferedImage img = swiftBot.getQRImage(); // Capture an image from SwiftBot's camera
            decodedMessage = swiftBot.decodeQRImage(img); // Attempt to decode a QR code

            if (!decodedMessage.isEmpty()) {
                // QR code successfully decoded
                System.out.println("\n---------------------------------------------------------------------");
                System.out.println("");
                System.out.println("QR Code found! Decoded message: " + decodedMessage);
                System.out.println("");
                System.out.println("---------------------------------------------------------------------");
                processQRCodeData(decodedMessage); // Process the extracted shape data
                return; // Exit the method as a QR code has been found
            }

            System.out.println("No QR Code found. Adjust the SwiftBot's camera.");
            try {
                Thread.sleep(1000); // Wait for 1 second before attempting again
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // If no QR code is detected within 10 seconds, return to the main menu
        System.out.println("Error: No QR code detected within 10 seconds. Returning to main menu...");
        try {
            mainMenu();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void processQRCodeData(String data) {
        String[] shapes = data.split("&"); // Split the QR code data into separate shape descriptions

        if (shapes.length > 5) {
            System.out.println("ERROR: You can only enter a maximum of 5 shapes.");
            return;
        }

        for (String shape : shapes) {
            if (shape.startsWith("S-")) { // Square detection
                String sideLengthStr = shape.substring(2);
                try {
                    int sideLength = Integer.parseInt(sideLengthStr);
                    if (sideLength >= 15 && sideLength <= 85) {
                        makeSquare(sideLength);
                    } else {
                        System.out.println("ERROR: Square side length must be between 15 and 85 cm.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("ERROR: Invalid input for square. (S-xx where xx is an integer between 15 and 85cm)");
                }
            } 
            // Triangle detection
            else if (shape.startsWith("T-")) {
                String[] sides = shape.substring(2).split("-");
                if (sides.length == 3) {
                    try {
                        int sideA = Integer.parseInt(sides[0]);
                        int sideB = Integer.parseInt(sides[1]);
                        int sideC = Integer.parseInt(sides[2]);

                        if (isValidTriangle(sideA, sideB, sideC)) {
                            makeTriangle(sideA, sideB, sideC);
                        } else {
                            System.out.println("ERROR: Invalid triangle.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("ERROR: Invalid triangle input. (Sides must be integers between 15-85)");
                    }
                } else {
                    System.out.println("ERROR: Triangle format incorrect. (T-xx-yy-zz, where xx, yy, and zz are integers between 15 and 85 cm.)");
                }
            } 
            // Hexagon detection
            else if (shape.startsWith("H-")) {
                String sideLengthStr = shape.substring(2);
                try {
                    int sideLength = Integer.parseInt(sideLengthStr);
                    if (sideLength >= 15 && sideLength <= 85) {
                        makeHexagon(sideLength);
                    } else {
                        System.out.println("ERROR: Hexagon side length must be between 15 and 85 cm.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("ERROR: Invalid input for hexagon. (H-xx where xx is an integer between 15 and 85 cm.)");
                }
            } 
            // Pentagon detection
            else if (shape.startsWith("P-")) {
                String sideLengthStr = shape.substring(2);
                try {
                    int sideLength = Integer.parseInt(sideLengthStr);
                    if (sideLength >= 15 && sideLength <= 85) {
                        makePentagon(sideLength);
                    } else {
                        System.out.println("ERROR: Pentagon side length must be between 15 and 85 cm.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("ERROR: Invalid input for pentagon. (P-xx where xx is an integer between 15 and 85 cm.)");
                }
            } 
            else {
                System.out.println("ERROR: Invalid shape format. It must begin with the first letter of the shape (Square, Triangle, Pentagon, or Hexagon.)");
                System.out.println("If you wish to do multiple shapes, input the data such that it appears as 'S-xx&S-yy' for example.");
            }
        }

        try {
            mainMenu(); // Return to main menu after processing all shapes
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Validate if three sides can form a triangle using the triangle inequality theorem
    public static boolean isValidTriangle(int a, int b, int c) {
        return (a + b > c) && (a + c > b) && (b + c > a);
    }

    public static void makeSquare(int sideLength) {
        try {
            long startTime = System.currentTimeMillis();
            long movementTime = calculateTimeForDistance(sideLength);

            if (movementTime == -1) return; // If invalid distance, do not proceed
            
            System.out.println("\n---------------------------------------------------------------------");
        	System.out.println("");
            System.out.println("Drawing a square with sides: " + sideLength + "x" + sideLength + "cm");
            System.out.println("");
        	System.out.println("---------------------------------------------------------------------");

            for (int i = 0; i < 4; i++) {
                // Move forward
                swiftBot.move(40, 40, (int) movementTime);  // Cast long to int here
                Thread.sleep(500);

                // Always turn 90 degrees after each side (adjusted for 115-degree turn behaviour)
                long turnTime = calculateTurnTime(90, 58);  // 90-degree turn
                swiftBot.move(0, 58, (int) turnTime);
                Thread.sleep(500);
            }

            long timeTaken = System.currentTimeMillis() - startTime;
            logger.logShape("Square", sideLength, timeTaken);

            // Blink green underlights after drawing the square
            Thread.sleep(1500);
            swiftBot.fillUnderlights(new int[]{0, 255, 0});
            Thread.sleep(2000);
            swiftBot.disableUnderlights();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //method that makes a triangle
    public static void makeTriangle(int sideA, int sideB, int sideC) {
        try {
            long startTime = System.currentTimeMillis();

            // Identify the longest side and reorder the sides
            int longestSide = Math.max(sideA, Math.max(sideB, sideC));
            int firstSide, secondSide, thirdSide;
            double angleA, angleB, angleC;

            // Rearranging the sides so that the longest side is always the first one
            if (longestSide == sideA) {
                firstSide = sideA;
                secondSide = sideB;
                thirdSide = sideC;
            } else if (longestSide == sideB) {
                firstSide = sideB;
                secondSide = sideA;
                thirdSide = sideC;
            } else {
                firstSide = sideC;
                secondSide = sideA;
                thirdSide = sideB;
            }

            // Calculate angles for the triangle using the Law of Cosines
            angleA = Math.toDegrees(Math.acos((Math.pow(secondSide, 2) + Math.pow(thirdSide, 2) - Math.pow(firstSide, 2)) / (2 * secondSide * thirdSide)));
            angleB = Math.toDegrees(Math.acos((Math.pow(firstSide, 2) + Math.pow(thirdSide, 2) - Math.pow(secondSide, 2)) / (2 * firstSide * thirdSide)));
            angleC = 180 - angleA - angleB; // Triangle angle sum = 180deg

            // Calculate the exterior angles (subtract interior from 180)
            double exteriorAngleA = 180 - angleA;
            double exteriorAngleB = 180 - angleB;
            double exteriorAngleC = 180 - angleC;

            System.out.println("\n---------------------------------------------------------------------");
        	System.out.println("");
            System.out.println("Drawing a triangle with sides: " + sideA + "cm, " + sideB + "cm, " + sideC + "cm");
            System.out.println("");
        	System.out.println("---------------------------------------------------------------------");
            // Calculate movement times for each side
            long movementTimeA = calculateTimeForDistance(firstSide);
            if (movementTimeA == -1) return;

            // Move forward for the first side (longest side)
            swiftBot.move(40, 40, (int) movementTimeA);  // Cast long to int here
            Thread.sleep(500);

            // Turn based on the exterior angle of the first side
            
            swiftBot.move(0, 58, 1200);
            Thread.sleep(500);

            long movementTimeB = calculateTimeForDistance(secondSide);
            if (movementTimeB == -1) return;

            // Move forward for the second side
            swiftBot.move(40, 40, (int) movementTimeB);  // Cast long to int here
            Thread.sleep(500);

            // Turn based on the exterior angle of the second side
            long turnTimeB = calculateTurnTime(exteriorAngleB, 58);
            swiftBot.move(0, 58, (int) turnTimeB);
            Thread.sleep(500);

            long movementTimeC = calculateTimeForDistance(thirdSide);
            if (movementTimeC == -1) return;

            // Move forward for the third side
            swiftBot.move(40, 40, (int) movementTimeC);  // Cast long to int here
            Thread.sleep(500);

            // Turn based on the exterior angle of the third side
            long turnTimeC = calculateTurnTime(exteriorAngleC, 58);
            swiftBot.move(0, 58, (int) turnTimeC);
            Thread.sleep(500);

            // Calculate total time taken to draw the triangle
            long timeTaken = System.currentTimeMillis() - startTime;
            logger.logShape("Triangle", sideA, sideB, sideC, exteriorAngleA, exteriorAngleB, exteriorAngleC, timeTaken);

            // Blink green underlights after drawing the triangle
            Thread.sleep(1500);
            swiftBot.fillUnderlights(new int[]{0, 255, 0});
            Thread.sleep(2000);
            swiftBot.disableUnderlights();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    public static void makePentagon(int sideLength) {
        try {
            long startTime = System.currentTimeMillis();
            long movementTime = calculateTimeForDistance(sideLength);

            if (movementTime == -1) return; // If invalid distance, do not proceed

            System.out.println("\n---------------------------------------------------------------------");
        	System.out.println("");
            System.out.println("Drawing a pentagon with sides: " + sideLength + "cm");
            System.out.println("");
        	System.out.println("---------------------------------------------------------------------");

            for (int i = 0; i < 5; i++) {
                // Move forward
                swiftBot.move(40, 40, (int) movementTime);  // Cast long to int here
                Thread.sleep(500);

                // Turn 72 degrees (adjusted for 115-degree turn behaviour)
                long turnTime = calculateTurnTime(72, 58);  // 72-degree turn
                swiftBot.move(0, 58, (int) turnTime);
                Thread.sleep(500);
            }

            long timeTaken = System.currentTimeMillis() - startTime;
            logger.logShape("Pentagon", sideLength, timeTaken);

            // Blink green underlights after drawing the pentagon
            Thread.sleep(1500);
            swiftBot.fillUnderlights(new int[]{0, 255, 0});
            Thread.sleep(2000);
            swiftBot.disableUnderlights();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void makeHexagon(int sideLength) {
        try {
            long startTime = System.currentTimeMillis();
            long movementTime = calculateTimeForDistance(sideLength);

            if (movementTime == -1) return; // If invalid distance, do not proceed

            System.out.println("\n---------------------------------------------------------------------");
        	System.out.println("");
            System.out.println("Drawing a hexagon with sides: " + sideLength + "cm");
            System.out.println("");
        	System.out.println("---------------------------------------------------------------------");
        	
            for (int i = 0; i < 6; i++) {
                // Move forward
                swiftBot.move(40, 40, (int) movementTime);  // Cast long to int here
                Thread.sleep(500);

                // Turn 60 degrees (adjusted for 115-degree turn behaviour)
                long turnTime = calculateTurnTime(60, 58);  // 60-degree turn
                swiftBot.move(0, 58, (int) turnTime);
                Thread.sleep(500);
            }

            long timeTaken = System.currentTimeMillis() - startTime;
            logger.logShape("Hexagon", sideLength, timeTaken);

            // Blink green underlights after drawing the hexagon
            Thread.sleep(1500);
            swiftBot.fillUnderlights(new int[]{0, 255, 0});
            Thread.sleep(2000);
            swiftBot.disableUnderlights();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Calculates the time required for the robot to move a specified distance
    public static long calculateTimeForDistance(int distance) {
        double speed = 12.33; // SwiftBot speed in cm/s when both wheels are at 40%

        // Ensure the distance is within the valid range (15 - 85 cm)
        if (distance < 15 || distance > 85) {
            System.out.println("ERROR: Distance must be between 15 and 85 cm.");
            return -1; // Return -1 to indicate an error
        }

        // Calculate time in seconds
        double timeInSeconds = distance / speed;

        // Convert time to milliseconds (since the SwiftBot API uses milliseconds)
        long movementTime = (long) (timeInSeconds * 1000);

        // Ensure movement time is greater than 0
        if (movementTime <= 0) {
            System.out.println("ERROR: Invalid movement time.");
            return -1; // Return -1 to indicate an error
        }

        return movementTime;
    }

    public static long calculateTurnTime(double angle, double speedPercentage) {
        // Time for 115-degree turn
        double timeFor115DegreeTurn = 1500;
        
        // Calculate the time for the specified angle
        double turnTime = (angle / 115) * timeFor115DegreeTurn;
        
        return (long) turnTime;
    }
}