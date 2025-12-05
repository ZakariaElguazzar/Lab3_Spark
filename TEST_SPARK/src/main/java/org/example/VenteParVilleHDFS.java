package org.example;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.List;

public class VenteParVilleHDFS {
    public static void main(String[] args) {
        // Configuration avec HDFS
        SparkConf conf = new SparkConf()
                .setAppName("VentesParVille-HDFS")
                .set("spark.hadoop.fs.defaultFS", "hdfs://namenode:8020")
                .set("spark.hadoop.dfs.replication", "1");

        // Utiliser YARN ou Spark standalone
        String master = args.length > 0 ? args[0] : "spark://spark-master:7077";
        conf.setMaster(master);

        JavaSparkContext sc = new JavaSparkContext(conf);

        try {
            // Chemins HDFS
            String inputPath = args.length > 1 ? args[1] : "hdfs://namenode:8020/ventes.txt";
            String outputPath = args.length > 2 ? args[2] : "hdfs://namenode:8020/ventes_par_ville";

            System.out.println("Lecture depuis: " + inputPath);
            System.out.println("Écriture vers: " + outputPath);

            // Charger depuis HDFS
            JavaRDD<String> ventesRDD = sc.textFile(inputPath);

            // Traitement
            JavaPairRDD<String, Double> ventesParVille = ventesRDD
                    .filter(ligne -> {
                        String[] elements = ligne.split(" ");
                        return elements.length == 4;
                    })
                    .mapToPair(ligne -> {
                        String[] elements = ligne.split(" ");
                        String ville = elements[1];
                        double prix = Double.parseDouble(elements[3]);
                        return new Tuple2<>(ville, prix);
                    })
                    .reduceByKey(Double::sum);

            // Afficher les résultats
            List<Tuple2<String, Double>> resultats = ventesParVille.collect();
            System.out.println("=== Total des ventes par ville ===");
            for (Tuple2<String, Double> resultat : resultats) {
                System.out.printf("%s: %.2f%n", resultat._1(), resultat._2());
            }

            // Sauvegarder sur HDFS
            ventesParVille.saveAsTextFile(outputPath);
            System.out.println("Résultats sauvegardés dans HDFS: " + outputPath);

        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
            e.printStackTrace();
        } finally {
            sc.close();
        }
    }
}
