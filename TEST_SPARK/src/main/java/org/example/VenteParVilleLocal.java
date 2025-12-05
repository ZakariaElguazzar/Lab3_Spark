package org.example;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.List;

public class VenteParVilleLocal {
    public static void main(String[] args) {
        // Configuration Spark en local
        SparkConf conf = new SparkConf()
                .setAppName("VentesParVille")
                .setMaster("local[*]");

        JavaSparkContext sc = new JavaSparkContext(conf);

        // Charger le fichier des ventes
        JavaRDD<String> ventesRDD = sc.textFile("/home/zakaria-el-guazzar/Documents/ventes.txt");

        // Transformer et calculer le total par ville
        JavaPairRDD<String, Double> ventesParVille = ventesRDD
                .filter(ligne -> ligne.split(" ").length == 4) // Filtrer d'abord les lignes valides
                .mapToPair(ligne -> {
                    String[] elements = ligne.split(" ");
                    String ville = elements[1];
                    double prix = Double.parseDouble(elements[3]);
                    return new Tuple2<>(ville, prix);
                })
                .reduceByKey(Double::sum); // Méthode référence pour plus de clarté

        // Afficher les résultats
        List<Tuple2<String, Double>> resultats = ventesParVille.collect();
        System.out.println("=== Total des ventes par ville ===");
        for (Tuple2<String, Double> resultat : resultats) {
            System.out.println(resultat._1() + ": " + String.format("%.2f", resultat._2()));
        }

        // Afficher le nombre de lignes traitées
        long nombreLignes = ventesRDD.count();
        long nombreLignesValides = ventesParVille.count();
        System.out.println("Lignes totales: " + nombreLignes);
        System.out.println("Villes distinctes: " + nombreLignesValides);

        // Sauvegarder le résultat
        ventesParVille.saveAsTextFile("resultats_ventes_par_ville_local");

        sc.close();
    }
}
