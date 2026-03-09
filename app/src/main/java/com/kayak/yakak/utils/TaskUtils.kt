package com.kayak.yakak.utils

import com.kayak.yakak.Task

private val taskList: MutableList<Task> = mutableListOf(
    Task(
        id = 1,
        name = "Faire les courses",
        description = "Acheter du lait, du pain, des œufs et des fruits.",
        isCompleted = false
    ),
    Task(
        id = 2,
        name = "Appeler le dentiste",
        description = "Prendre rendez-vous pour le contrôle annuel.",
        isCompleted = false
    ),
    Task(
        id = 3,
        name = "Séance de sport",
        description = "45 minutes de cardio et renforcement musculaire.",
        isCompleted = true
    ),
/*    Task(
        id = 4,
        name = "Lire un livre",
        description = "Lire au moins 20 pages du roman en cours.",
        isCompleted = false
    ),
    Task(
        id = 5,
        name = "Nettoyer la cuisine",
        description = "Vider le lave-vaisselle et nettoyer les plans de travail.",
        isCompleted = false
    ),
    Task(
        id = 6,
        name = "Sortir les poubelles",
        description = "Ne pas oublier les bacs de recyclage ce soir.",
        isCompleted = true
    ),
    Task(
        id = 7,
        name = "Préparer le dîner",
        description = "Cuisiner une recette saine pour toute la famille.",
        isCompleted = true
    ),
    Task(
        id = 8,
        name = "Arroser les plantes",
        description = "Vérifier l'humidité du terreau pour les plantes du salon.",
        isCompleted = true
    ),
    Task(
        id = 9,
        name = "Méditation",
        description = "10 minutes de pleine conscience pour se relaxer.",
        isCompleted = false
    ),
    Task(
        id = 10,
        name = "Réviser le projet Yakak",
        description = "Relire le code et planifier les prochaines fonctionnalités.",
        isCompleted = false
    ),*/
    /*Task(
        id = 11,
        name = "Promener le chien",
        description = "30 minutes de promenade dans le parc.",
        isCompleted = false
    ),
    Task(
        id = 12,
        name = "Payer les factures",
        description = "Payer les factures d'électricité et d'internet.",
        isCompleted = false
    ),
    Task(
        id = 13,
        name = "Organiser le bureau",
        description = "Ranger les documents et nettoyer l'espace de travail.",
        isCompleted = false
    ),
    Task(
        id = 14,
        name = "Laver le linge",
        description = "Trier les couleurs et lancer une machine.",
        isCompleted = false
    ),
    Task(
        id = 15,
        name = "Regarder un film",
        description = "Choisir un film à regarder ce soir.",
        isCompleted = false
    ),
    Task(
        id = 16,
        name = "Apprendre une nouvelle compétence",
        description = "Regarder un tutoriel sur un sujet d'intérêt.",
        isCompleted = false
    ),
    Task(
        id = 17,
        name = "Faire une sieste",
        description = "20 minutes pour recharger les batteries.",
        isCompleted = true
    ),
    Task(
        id = 18,
        name = "Écrire un journal",
        description = "Noter les pensées et les événements de la journée.",
        isCompleted = false
    ),
    Task(
        id = 19,
        name = "Appeler un ami",
        description = "Prendre des nouvelles d'un ami proche.",
        isCompleted = false
    ),
    Task(
        id = 20,
        name = "Planifier le week-end",
        description = "Décider des activités et des sorties pour le week-end.",
        isCompleted = false
    )*/
)

fun getTaskList(): List<Task> {
    return taskList
}