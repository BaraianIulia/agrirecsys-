import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class ClosestNeighborCalculator {

    // Class to hold information of each farmer from the CSV file
    static class Farmer {
        int id;
        String soilType;
        String fertilizerType;
        String climate;
        double humidityLevel;
        String pestManagement;
        String plantTime;
        String cropHarvested;
        double waterTemperature;
        String harvestColour;
        String seedSupplier;
        String season;
        double distanceToRetailer;
        double harvestYield;

        Farmer(int id, String soilType, String fertilizerType, String climate, double humidityLevel, String pestManagement,
               String plantTime, String cropHarvested, double waterTemperature, String harvestColour,
               String seedSupplier, String season, double distanceToRetailer, double harvestYield) {
            this.id = id;
            this.soilType = soilType;
            this.fertilizerType = fertilizerType;
            this.climate = climate;
            this.humidityLevel = humidityLevel;
            this.pestManagement = pestManagement;
            this.plantTime = plantTime;
            this.cropHarvested = cropHarvested;
            this.waterTemperature = waterTemperature;
            this.harvestColour = harvestColour;
            this.seedSupplier = seedSupplier;
            this.season = season;
            this.distanceToRetailer = distanceToRetailer;
            this.harvestYield = harvestYield;
        }
    }

    // Method to calculate total distance between two farmers
    public static double calculateTotalDistance(Farmer query, Farmer comparison) {
        int hammingDistance = 0;

        // Categorical distance (Hamming)
        if (!query.soilType.equals(comparison.soilType)) hammingDistance += 1;
        if (!query.fertilizerType.equals(comparison.fertilizerType)) hammingDistance += 1;
        if (!query.climate.equals(comparison.climate)) hammingDistance += 1;
        if (!query.pestManagement.equals(comparison.pestManagement)) hammingDistance += 1;
        if (!query.plantTime.equals(comparison.plantTime)) hammingDistance += 1;
        if (!query.cropHarvested.equals(comparison.cropHarvested)) hammingDistance += 1;
        if (!query.harvestColour.equals(comparison.harvestColour)) hammingDistance += 1;
        if (!query.seedSupplier.equals(comparison.seedSupplier)) hammingDistance += 1;
        if (!query.season.equals(comparison.season)) hammingDistance += 1;

        // Numerical distance (Euclidean)
        double euclideanDistance = Math.pow(query.humidityLevel - comparison.humidityLevel, 2)
                + Math.pow(query.waterTemperature - comparison.waterTemperature, 2)
                + Math.pow(query.distanceToRetailer - comparison.distanceToRetailer, 2)
                + Math.pow(query.harvestYield - comparison.harvestYield, 2);

        euclideanDistance = Math.sqrt(euclideanDistance);

        // Combine both distances
        return hammingDistance + euclideanDistance;
    }

    // Method to find the k-nearest neighbors for each farmer
    public static void findKNearestNeighbors(String filePath, int k) {
        List<Farmer> farmers = new ArrayList<>();

        // Reading data from CSV file
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // Skip header row
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");

                // Parsing each field according to the column order from your dataset
                int id = Integer.parseInt(values[0].trim());
                String soilType = values[1].trim();
                String fertilizerType = values[2].trim();
                String climate = values[3].trim();
                double humidityLevel = Double.parseDouble(values[4].trim());
                String pestManagement = values[5].trim();
                String plantTime = values[6].trim();
                String cropHarvested = values[7].trim();
                double waterTemperature = Double.parseDouble(values[8].trim().replace("C", ""));
                String harvestColour = values[9].trim();
                String seedSupplier = values[10].trim();
                String season = values[11].trim();
                double distanceToRetailer = Double.parseDouble(values[12].trim().replace("km", ""));
                double harvestYield = Double.parseDouble(values[13].trim().replace("%", ""));

                farmers.add(new Farmer(id, soilType, fertilizerType, climate, humidityLevel, pestManagement,
                        plantTime, cropHarvested, waterTemperature, harvestColour,
                        seedSupplier, season, distanceToRetailer, harvestYield));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("Error parsing a numeric value. Please check the data format.");
            e.printStackTrace();
        }

        // For each farmer, find the k-nearest neighbors
        for (Farmer query : farmers) {
            PriorityQueue<FarmerDistance> nearestNeighbors = new PriorityQueue<>((a, b) -> Double.compare(b.distance, a.distance));

            for (Farmer comparison : farmers) {
                if (query.id == comparison.id) continue; // Skip self-comparison
                double distance = calculateTotalDistance(query, comparison);

                if (nearestNeighbors.size() < k) {
                    nearestNeighbors.add(new FarmerDistance(comparison, distance));
                } else if (distance < nearestNeighbors.peek().distance) {
                    nearestNeighbors.poll();
                    nearestNeighbors.add(new FarmerDistance(comparison, distance));
                }
            }

//            // Print the k-nearest neighbors for the current farmer
//            System.out.println("Farmer ID: " + query.id + " - " + k + " Nearest Neighbors:");
//            for (FarmerDistance neighbor : nearestNeighbors) {
//                System.out.println("  Neighbor ID: " + neighbor.farmer.id + " - Distance: " + neighbor.distance);
//            }
        }
    }

    // Helper class to store a farmer and its distance to the query farmer
    static class FarmerDistance {
        Farmer farmer;
        double distance;

        FarmerDistance(Farmer farmer, double distance) {
            this.farmer = farmer;
            this.distance = distance;
        }
    }

    public static void main(String[] args) {

        long startTime = System.nanoTime(); // Start time

        String filePath = "simple_agriculture_dataset95000.csv";
        int k = 3; // Set the desired number of neighbors
        findKNearestNeighbors(filePath, k);


        long endTime = System.nanoTime(); // End time
        double duration = (endTime - startTime) / 1_000_000_000.0; // Convert to seconds

        System.out.println("\nExecution Time for KNN: " + duration + " seconds");
    }
}
