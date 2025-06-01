import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

public class LSH {

    private final int numHashFunctions;
    private final int numBands;
    private final double[][] randomVectors;
    private final List<ConcurrentHashMap<Integer, List<DataPoint>>> hashTables;

    public LSH(int numHashFunctions, int numBands) {
        this.numHashFunctions = numHashFunctions;
        this.numBands = numBands;
        this.hashTables = new ArrayList<>();
        this.randomVectors = new double[numBands][numHashFunctions];

        Random rand = new Random(42); // Fixed seed for consistency

        // Precompute random vectors for each band
        for (int i = 0; i < numBands; i++) {
            for (int j = 0; j < numHashFunctions; j++) {
                randomVectors[i][j] = rand.nextDouble() - 0.5;
            }
            hashTables.add(new ConcurrentHashMap<>());
        }
    }

    private int hash(DataPoint point, int band) {
        double[] values = {point.wateringTemperature, point.humidityLevel, point.harvestYield, point.distanceToRetailer};
        double dotProduct = 0.0;

        for (int i = 0; i < numHashFunctions; i++) {
            dotProduct += values[i % values.length] * randomVectors[band][i];
        }

        // Convert to a stable hash value
        return Objects.hash((int) (dotProduct * 1000)); // Fixed scale factor for better bucketing
    }

    public void addDataPoint(DataPoint point) {
        IntStream.range(0, numBands).parallel().forEach(band -> {
            int hashKey = hash(point, band);
            hashTables.get(band).computeIfAbsent(hashKey, k -> new ArrayList<>()).add(point);
        });
    }

    public List<DataPoint> getApproximateNeighbors(DataPoint query) {
        Set<DataPoint> candidates = new HashSet<>();

        for (int band = 0; band < numBands; band++) {
            int hashKey = hash(query, band);
            candidates.addAll(hashTables.get(band).getOrDefault(hashKey, new ArrayList<>()));
        }

        return new ArrayList<>(candidates);
    }

    static class DataPoint {
        int id;
        String soilType, fertilizerType, climate, pestDiseaseManagement, plantTime, cropHarvested;
        double wateringTemperature, humidityLevel, distanceToRetailer, harvestYield;
        String harvestColour, seedSupplier, season;

        public DataPoint(int id, String soilType, String fertilizerType, String climate, String pestDiseaseManagement,
                         String plantTime, String cropHarvested, double wateringTemperature, String harvestColour,
                         double humidityLevel, String seedSupplier, String season, double distanceToRetailer, double harvestYield) {
            this.id = id;
            this.soilType = soilType;
            this.fertilizerType = fertilizerType;
            this.climate = climate;
            this.pestDiseaseManagement = pestDiseaseManagement;
            this.plantTime = plantTime;
            this.cropHarvested = cropHarvested;
            this.wateringTemperature = wateringTemperature;
            this.harvestColour = harvestColour;
            this.humidityLevel = humidityLevel;
            this.seedSupplier = seedSupplier;
            this.season = season;
            this.distanceToRetailer = distanceToRetailer;
            this.harvestYield = harvestYield;
        }

        public double distanceTo(DataPoint other) {
            return Math.sqrt(Math.pow(this.wateringTemperature - other.wateringTemperature, 2) +
                    Math.pow(this.humidityLevel - other.humidityLevel, 2) +
                    Math.pow(this.harvestYield - other.harvestYield, 2) +
                    Math.pow(this.distanceToRetailer - other.distanceToRetailer, 2));
        }
    }

    public static List<DataPoint> loadCSV(String filePath) throws IOException {
        List<DataPoint> dataPoints = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line;
        br.readLine(); // Skip header

        while ((line = br.readLine()) != null) {
            String[] values = line.split(",");
            int id = Integer.parseInt(values[0].trim());
            String soilType = values[1].trim();
            String fertilizerType = values[2].trim();
            String climate = values[3].trim();
            double humidityLevel = Double.parseDouble(values[4].trim());
            String pestDiseaseManagement = values[5].trim();
            String plantTime = values[6].trim();
            String cropHarvested = values[7].trim();
            double wateringTemperature = Double.parseDouble(values[8].trim().replace("C", ""));
            String harvestColour = values[9].trim();
            String seedSupplier = values[10].trim();
            String season = values[11].trim();
            double distanceToRetailer = Double.parseDouble(values[12].trim().replace("km", ""));
            double harvestYield = Double.parseDouble(values[13].trim().replace("%", ""));

            dataPoints.add(new DataPoint(id, soilType, fertilizerType, climate, pestDiseaseManagement, plantTime,
                    cropHarvested, wateringTemperature, harvestColour, humidityLevel, seedSupplier, season, distanceToRetailer, harvestYield));
        }
        br.close();
        return dataPoints;
    }

    public static void main(String[] args) {
        try {
            LSH ann = new LSH(10, 5); // Increased hash functions & bands for better accuracy

            long startTime = System.nanoTime();
            List<DataPoint> dataPoints = loadCSV("C:\\Users\\iulia\\Downloads\\agrirecsys--main\\simple_agriculture_dataset100000.csv");

            dataPoints.parallelStream().forEach(ann::addDataPoint);

            for (DataPoint query : dataPoints) {
                List<DataPoint> neighbors = ann.getApproximateNeighbors(query);

                PriorityQueue<DataPointDistance> nearestNeighbors = new PriorityQueue<>(Comparator.comparingDouble(a -> a.distance));

                for (DataPoint neighbor : neighbors) {
                    if (neighbor.id != query.id) {
                        double distance = query.distanceTo(neighbor);
                        if (nearestNeighbors.size() < 3) {
                            nearestNeighbors.add(new DataPointDistance(neighbor, distance));
                        } else if (distance < nearestNeighbors.peek().distance) {
                            nearestNeighbors.poll();
                            nearestNeighbors.add(new DataPointDistance(neighbor, distance));
                        }
                    }
                }

                // Ensure exactly 3 nearest neighbors
                while (nearestNeighbors.size() < 3) {
                    nearestNeighbors.add(new DataPointDistance(new DataPoint(-1, "N/A", "N/A", "N/A", "N/A", "N/A", "N/A",
                            0, "N/A", 0, "N/A", "N/A", 0, 0), Double.MAX_VALUE));
                }

                // Print the 3 closest neighbors for the current query point
                //  System.out.println("Data Point ID: " + query.id + " - 3 Nearest Neighbors:");
                for (DataPointDistance neighbor : nearestNeighbors) {
                    if (neighbor.dataPoint.id != -1) {  // Avoid printing dummy neighbors
                        //  System.out.println("  Neighbor ID: " + neighbor.dataPoint.id + " - Distance: " + neighbor.distance);
                    }
                }
            }

            long endTime = System.nanoTime();
            double duration = (endTime - startTime) / 1_000_000_000.0;
            System.out.println("\nExecution Time for LSH: " + duration + " seconds");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class DataPointDistance {
        DataPoint dataPoint;
        double distance;

        DataPointDistance(DataPoint dataPoint, double distance) {
            this.dataPoint = dataPoint;
            this.distance = distance;
        }
    }
}
