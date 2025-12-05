package org.example;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.List;

public class VenteParVilleAnneeLocal {
    public static void main(String[] args) {
        // Configuration Spark en local
        SparkConf conf = new SparkConf()
                .setAppName("VentesParVilleEtAnnee")
                .setMaster("local[*]");

        JavaSparkContext sc = new JavaSparkContext(conf);

        // Charger le fichier des ventes
        JavaRDD<String> ventesRDD = sc.textFile("/home/zakaria-el-guazzar/Documents/ventes.txt");

        // Transformer et calculer le total par ville et année
        JavaPairRDD<String, Double> ventesParVilleAnnee = ventesRDD
                .filter(ligne -> {
                    String[] elements = ligne.split(" ");
                    return elements.length == 4;
                })
                .mapToPair(ligne -> {
                    String[] elements = ligne.split(" ");
                    String ville = elements[1];
                    String date = elements[0];  // Date est maintenant le premier élément
                    double prix = Double.parseDouble(elements[3]);

                    // Extraire l'année de la date (format: AAAA-MM-JJ)
                    String annee = "Inconnue";
                    try {
                        String[] dateParts = date.split("-");
                        if (dateParts.length >= 1) {
                            annee = dateParts[0];  // L'année est le premier élément en format AAAA-MM-JJ
                        }
                    } catch (Exception e) {
                        System.err.println("Erreur d'extraction de date pour: " + ligne);
                    }

                    // Clé composite: "Ville-Année"
                    String cleComposite = ville + "-" + annee;

                    return new Tuple2<>(cleComposite, prix);
                })
                .reduceByKey(Double::sum);

        // Afficher les résultats triés
        List<Tuple2<String, Double>> resultats = ventesParVilleAnnee.collect();

        System.out.println("=== Total des ventes par ville et par année ===");
        resultats.stream()
                .sorted((r1, r2) -> r1._1().compareTo(r2._1()))
                .forEach(resultat ->
                        System.out.printf("%s: %.2f%n", resultat._1(), resultat._2())
                );

        // Version alternative avec structure hiérarchique
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

        // Afficher un résumé par année
        JavaPairRDD<String, Double> ventesParAnnee = ventesRDD
                .filter(ligne -> ligne.split(" ").length == 4)
                .mapToPair(ligne -> {
                    String[] elements = ligne.split(" ");
                    String date = elements[0];
                    double prix = Double.parseDouble(elements[3]);

                    String annee = "Inconnue";
                    try {
                        String[] dateParts = date.split("-");
                        if (dateParts.length >= 1) {
                            annee = dateParts[0];
                        }
                    } catch (Exception e) {
                        // Ignorer les erreurs
                    }

                    return new Tuple2<>(annee, prix);
                })
                .reduceByKey(Double::sum)
                .sortByKey();

        System.out.println("\n=== Total des ventes par année ===");
        List<Tuple2<String, Double>> resultatsAnnee = ventesParAnnee.collect();
        for (Tuple2<String, Double> resultat : resultatsAnnee) {
            System.out.printf("Année %s: %.2f%n", resultat._1(), resultat._2());
        }

        // Afficher des statistiques
        long nombreLignes = ventesRDD.count();
        long nombreLignesTraitees = ventesRDD
                .filter(ligne -> {
                    String[] parts = ligne.split(" ");
                    if (parts.length != 4) return false;
                    try {
                        Double.parseDouble(parts[3]);
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                })
                .count();

        System.out.println("\n=== Statistiques ===");
        System.out.println("Lignes totales: " + nombreLignes);
        System.out.println("Lignes valides traitées: " + nombreLignesTraitees);
        System.out.println("Combinaisons ville-année: " + ventesParVilleAnnee.count());

        // Afficher quelques exemples de données traitées
        System.out.println("\n=== Exemples de données ===");
        List<String> exemples = ventesRDD.take(3);
        for (int i = 0; i < exemples.size(); i++) {
            System.out.println("Exemple " + (i+1) + ": " + exemples.get(i));
        }

        // Sauvegarder les résultats
        ventesParVilleAnnee.saveAsTextFile("resultats_ventes_par_ville_annee_local");

        sc.close();
    }
}