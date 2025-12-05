package org.example;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.Arrays;
import java.util.List;

public class LogAnalyzer {
    public static void main(String[] args) {

        // Configuration Spark
        SparkConf conf = new SparkConf()
                .setAppName("Simple Log Analyzer")
                .setMaster("local[*]");

        JavaSparkContext sc = new JavaSparkContext(conf);

        try {
            System.out.println("=== ANALYSE DE LOGS APACHE ===");

            // 1. Lecture des données
            JavaRDD<String> logs = sc.textFile("/home/zakaria-el-guazzar/Desktop/TP_3_BIG_DATA/access.log");

            // 2. Extraction et comptage simple
            JavaRDD<String[]> parsed = logs.map(line -> {
                try {
                    // Diviser la ligne
                    String[] parts = line.split(" ");
                    if (parts.length >= 9) {
                        // IP est le premier élément
                        String ip = parts[0];

                        // Trouver la méthode HTTP (après le premier guillemet)
                        String method = "";
                        String resource = "";
                        for (int i = 0; i < parts.length; i++) {
                            if (parts[i].startsWith("\"")) {
                                method = parts[i].replace("\"", "");
                                if (i + 1 < parts.length) {
                                    resource = parts[i + 1];
                                }
                                break;
                            }
                        }

                        // Trouver le code HTTP (chercher un nombre de 3 chiffres)
                        String code = "";
                        for (String part : parts) {
                            if (part.matches("\\d{3}")) {
                                code = part;
                                break;
                            }
                        }

                        return new String[]{ip, method, resource, code};
                    }
                } catch (Exception e) {
                    // Ignorer les erreurs
                }
                return new String[]{"", "", "", ""};
            }).filter(parts -> !parts[3].isEmpty());

            // 3. Statistiques
            long total = parsed.count();
            System.out.println("\n1. STATISTIQUES GLOBALES");
            System.out.println("   Total des requêtes: " + total);

            // 4. Top IPs
            System.out.println("\n2. TOP 5 IPs");
            parsed.mapToPair(parts -> new Tuple2<>(parts[0], 1))
                    .reduceByKey(Integer::sum)
                    .mapToPair(Tuple2::swap)
                    .sortByKey(false)
                    .take(5)
                    .forEach(tuple -> {
                        System.out.println("   " + tuple._2() + ": " + tuple._1() + " requêtes");
                    });

            // 5. Top ressources
            System.out.println("\n3. TOP 5 RESSOURCES");
            parsed.mapToPair(parts -> new Tuple2<>(parts[2], 1))
                    .reduceByKey(Integer::sum)
                    .mapToPair(Tuple2::swap)
                    .sortByKey(false)
                    .take(5)
                    .forEach(tuple -> {
                        System.out.println("   " + tuple._2() + ": " + tuple._1() + " requêtes");
                    });

            // 6. Codes HTTP
            System.out.println("\n4. CODES HTTP");
            parsed.mapToPair(parts -> new Tuple2<>(parts[3], 1))
                    .reduceByKey(Integer::sum)
                    .sortByKey()
                    .collect()
                    .forEach(tuple -> {
                        double percent = (double) tuple._2() / total * 100;
                        System.out.printf("   Code %s: %d (%.1f%%)\n",
                                tuple._1(), tuple._2(), percent);
                    });

            System.out.println("\n=== ANALYSE TERMINÉE ===");

        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
        } finally {
            sc.close();
        }
    }
}