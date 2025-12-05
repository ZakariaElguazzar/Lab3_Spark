package org.example;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.List;

public class VenteParVilleAnneeHDFS {
    public static void main(String[] args) {
        // Configuration avec HDFS
        SparkConf conf = new SparkConf()
                .setAppName("VentesParVilleEtAnnee-HDFS")
                .set("spark.hadoop.fs.defaultFS", "hdfs://namenode:8020")
                .set("spark.hadoop.dfs.replication", "1");

        // Utiliser YARN ou Spark standalone
        String master = args.length > 0 ? args[0] : "spark://spark-master:7077";
        conf.setMaster(master);

        JavaSparkContext sc = new JavaSparkContext(conf);

        try {
            // Chemins HDFS
            String inputPath = args.length > 1 ? args[1] : "hdfs://namenode:8020/ventes.txt";
            String outputPath = args.length > 2 ? args[2] : "hdfs://namenode:8020/ventes_par_ville_annee";

            System.out.println("Lecture depuis: " + inputPath);
            System.out.println("Écriture vers: " + outputPath);

            // Charger depuis HDFS
            JavaRDD<String> ventesRDD = sc.textFile(inputPath);

            // Traitement: Clé composite (Ville + Année)
            JavaPairRDD<String, Double> ventesParVilleAnnee = ventesRDD
                    .filter(ligne -> {
                        String[] elements = ligne.split(" ");
                        return elements.length == 4;
                    })
                    .mapToPair(ligne -> {
                        String[] elements = ligne.split(" ");
                        String ville = elements[1];
                        String date = elements[2]; // Format attendu: JJ/MM/AAAA

                        // Extraire l'année de la date
                        String annee;
                        try {
                            String[] dateParts = date.split("/");
                            if (dateParts.length == 3) {
                                annee = dateParts[2]; // Année est le troisième élément
                            } else {
                                annee = "Inconnue";
                            }
                        } catch (Exception e) {
                            annee = "Inconnue";
                        }

                        double prix = Double.parseDouble(elements[3]);
                        // Clé composite: "Ville-Année"
                        String cleComposite = ville + "-" + annee;

                        return new Tuple2<>(cleComposite, prix);
                    })
                    .reduceByKey(Double::sum);

            // Afficher les résultats
            List<Tuple2<String, Double>> resultats = ventesParVilleAnnee.collect();
            System.out.println("=== Total des ventes par ville et par année ===");
            for (Tuple2<String, Double> resultat : resultats) {
                System.out.printf("%s: %.2f%n", resultat._1(), resultat._2());
            }

            // Alternative: Structure hiérarchique Tuple2<Annee, Tuple2<Ville, Prix>>
            JavaPairRDD<String, Tuple2<String, Double>> ventesStructurees = ventesParVilleAnnee
                    .mapToPair(tuple -> {
                        String[] parts = tuple._1().split("-");
                        String ville = parts[0];
                        String annee = parts.length > 1 ? parts[1] : "Inconnue";
                        return new Tuple2<>(annee, new Tuple2<>(ville, tuple._2()));
                    })
                    .sortByKey();

            System.out.println("\n=== Structure hiérarchique (Année > Ville) ===");
            List<Tuple2<String, Tuple2<String, Double>>> resultatsStructurees = ventesStructurees.collect();
            for (Tuple2<String, Tuple2<String, Double>> resultat : resultatsStructurees) {
                System.out.printf("Année %s - %s: %.2f%n",
                        resultat._1(),
                        resultat._2()._1(),
                        resultat._2()._2());
            }

            // Sauvegarder sur HDFS
            ventesParVilleAnnee.saveAsTextFile(outputPath);
            System.out.println("\nRésultats sauvegardés dans HDFS: " + outputPath);

        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
            e.printStackTrace();
        } finally {
            sc.close();
        }
    }
}
