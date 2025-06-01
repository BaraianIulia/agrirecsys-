import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class AgricultureDatasetGenerator {

    public static void main(String[] args) {
        String csvFile = "simple_agriculture_dataset100000.csv";
        try (FileWriter writer = new FileWriter(csvFile)) {
            // Header row
            writer.append("id,soil_type,fertilizer_type,climate,humidity_level,pest_disease_management,");
            writer.append("plant_time,crop_harvested,water_temperature,harvest_colour,");
            writer.append("seed_supplier,season,distance_to_retailer,harvest_yield\n");

            // Possible values for each category
            String[] soilTypes = {"Sandy", "Clay", "Saline", "Loamy"};
            String[] fertilizerTypes = {"Organic", "Chemical", "Compost"};
            String[] climates = {"Tropical", "Temperate", "Arid"};
            String[] pestDiseaseManagement = {"Chemical Control - Synthetic Pesticides",
                    "Chemical Control - Organic Pesticides",
                    "Biological Control - Predatory Insects",
                    "Integrated Pest Management"};
            String[] seedSuppliers = {"AgriSeeds Co.", "FarmGrow Inc.", "GreenFields", "CropLife Solutions"};
            String[] seasons = {"Spring", "Summer", "Autumn", "Winter"};
            String[] plantTimes = {"Early Spring", "Late Spring", "Early Summer", "Late Summer", "Autumn", "Winter"};
            String[] harvestColours = {"Rare", "Medium-Rare", "Medium", "Medium-Well", "Well-Done", "Overdone"};
            String[] crops = {"Tomato", "Carrot", "Wheat", "Corn", "Lettuce", "Potato"}; // Example crop types

            // Random number generator
            Random rand = new Random();

            // Generate X rows of data
            for (int i = 1; i <= 100000; i++) {
                String id = String.valueOf(i);
                String soilType = soilTypes[rand.nextInt(soilTypes.length)];
                String fertilizerType = fertilizerTypes[rand.nextInt(fertilizerTypes.length)];
                String climate = climates[rand.nextInt(climates.length)];
                String humidityLevel = String.valueOf(rand.nextInt(26) + 60);  // 60% to 85%
                String pestManagement = pestDiseaseManagement[rand.nextInt(pestDiseaseManagement.length)];
                String plantTime = plantTimes[rand.nextInt(plantTimes.length)]; // Added plant time
                String cropHarvested = crops[rand.nextInt(crops.length)]; // Random crop
                String waterTemperature = String.valueOf(rand.nextInt(15) + 20) + "C";  // 20 to 35°C
                String harvestColour = harvestColours[rand.nextInt(harvestColours.length)];
                String seedSupplier = seedSuppliers[rand.nextInt(seedSuppliers.length)];
                String season = seasons[rand.nextInt(seasons.length)];
                String distanceToRetailer = (rand.nextInt(7) + 128) + "km";
                String harvestYield = String.valueOf(rand.nextInt(81) + 20) + "%"; // 20% - 100%

                // Create the row
                writer.append(id).append(",")
                        .append(soilType).append(",")
                        .append(fertilizerType).append(",")
                        .append(climate).append(",")
                        .append(humidityLevel).append(",")
                        .append(pestManagement).append(",")
                        .append(plantTime).append(",")
                        .append(cropHarvested).append(",")
                        .append(waterTemperature).append(",")
                        .append(harvestColour).append(",")
                        .append(seedSupplier).append(",")
                        .append(season).append(",")
                        .append(distanceToRetailer).append(",")
                        .append(harvestYield).append("\n");
            }

            System.out.println("CSV file created successfully!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
