Json Web Service
================

Cette page décrit les methodes disponible dans le Web Service JSON.

Par défaut, le point d'entrée de ce service est: [http://127.0.0.1:9091/cluster](http://127.0.0.1:9091/cluster)

GET /cluster : vue générale du cluster
--------------------------------------

Cette méthode renvoit des informations générale sur le cluster.
Les informations du noeud interrogé (point d'entrée, identifiant) et les informations des noeuds qu'il a collecté.

Pour chaque noeud, on a la liste des services qu'il sert et les groupes auquel il appartient.

- Method HTTP: GET
- Path: /cluster/

```bash
curl -XGET "http://127.0.0.1:9091/cluster"
```

Structure JSON retournée:

```json
{
  "me" : "http://192.168.56.1:9091",
  "uuid" : "30a2ef64-958a-11e8-bec8-6c3be50f2a3d",
  "active_nodes" : {
    "http://192.168.56.1:9091" : {
      "address" : "http://192.168.56.1:9091",
      "node_live_id" : "30a2ef64-958a-11e8-bec8-6c3be50f2a3d",
      "services" : [ "cluster" ],
      "groups" : [ ]
    }
  },
  "groups" : { },
  "services" : {
    "cluster" : "ok"
  },
  "last_keep_alive_execution" : "2018-08-01T12:55:38.926+0000"
}
```

- me: Adresse public du noeud.
- uuid: Identifiant unique du noeud. Cet identifiant est dynamiquement généré au lancement du service.
- actives_nodes: Liste des noeuds connus. Pour chaque noeud actif on a:
  - address: L'addresse du noeud.
  - node_live_id: L'dentifiant unique du noeud.
  - services: La liste des services servit par le noeud.
  - groups: La liste des groupes auquel appartient le noeud.
 - groups: La liste des groupes auquel appartient ce noeud.
 - services: La liste des services servit par ce noeud.
 - last_keep_alive_execution: La dernière fois que ce noeud a envoyé un message "alive" au cluster.
