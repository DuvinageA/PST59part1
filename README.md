# Application mobile

## Modifications à apporter

#### 1) Django

La valeur statique IP_ADRESS doit être modifiée dans le code des deux activités (MainActivity et TreatmentActivity) pour correspondre à celle du serveur django. Cela permettra de faire les requêtes GET, POST et PUT.
  
#### 2) Dropbox

Pour pouvoir envoyer les images sur Dropbox, il faut d'abord créer une nouvelle application à partir du lien suivant : https://www.dropbox.com/developers/apps en cliquant sur Create app puis en choisissant Dropbox API, App folder et en saisissant le nom de l'application à créer. Une fois ceci réalisé, il faudra cliquer sur Generate dans la section Generate access token dans les options de l'application Dropbox et remplacer la valeur statique ACCESS_TOKEN dans le code des deux activités (MainActivity et TreatmentActivity). Cela permettra d'accéder au dossier de l'application Dropbox.

#### 3) Autorisations

Lors de la première installation de l'application, il faut autoriser manuellement le Stockage (pour pouvoir stocket les photos) et l'accès au Microphone (pour pouvoir donner le texte oralement). 


## Utilisation

Le service Django REST doit être en place et les modifications précédemment citées doivent avoir été apportées. Au lancement de l'application, une requête est faite au service REST pour afficher les images déjà présentes. Bien sûr, rien ne s'affiche lors du premier lancement. On peut cependant appuyer sur le bouton en bas à droite pour prendre une photo. 

Deux choix sont proposés à l'utilisateur, choisir l'image depuis la galerie (la galerie prend la main, l'utilisateur peut sélectionner une photo présente sur son téléphone) ou prendre une photo avec la caméra du téléphone (l'appareil photo prend alors la main, l'utilisateur peut prendre une photo puis valider son choix).

L'activité de traitement est alors lancée, affichant l'image. Celle-ci peut être agrandie/rétrécie en utilisant les deux doigts et en zoomant/dézoomant ou déplacée en bougant avec un doigt. Si l'utilisateur maintient son doigt appuyé sur l'image un certain temps, l'image devient sombre. S'il déplace son doigt, un rectange apparaît ; il s'agit de la sélection de la zone à décrire. Une fois que l'utilisateur relâche son appui, la zone apparaît avec des "ronds" sur ses coins. En appuyant sur les ronds et en se déplaçant, l'utilisateur peut à nouveau éditer la sélection. En appuyant sur le bouton en bas à droite, l'utilisateur peut saisir la description de la zone sélectionnée. Une fonctionnalité "push-to-talk" a été implémentée, il suffit pour cela de maintenir le bouton de micro enfoncé et de prononcer la description. Le texte s'affiche une fois le bouton relâché. Une fois la description entièrement saisie, on peut valider et revenir sur l'image. La sélection s'affiche désormais en rouge, on peut cliquer sur le rectangle pour revenir en mode édition ou créer une nouvelle sélection.

Une fois l'édition terminer, l'envoi se fait via la sélection du bouton "Envoyer" dans la barre d'action, en haut de l'écran. Un chargement a alors lieu le temps d'envoyer l'image sur le Dropbox et les informations vers le service REST. Une fois le chargement terminé, un retour vers l'activité principale a lieu et la nouvelle image s'affiche, elle peut ainsi être à nouveau éditée en cliquant dessus.

## Liens utiles

https://www.dropbox.com/developers-v1/core/start/android

https://github.com/dropbox/dropbox-sdk-java#dropbox-for-java-tutorial
