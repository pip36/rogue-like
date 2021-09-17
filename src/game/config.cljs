(ns game.config)

(def input-keys {38 :UP
                 40 :DOWN
                 37 :LEFT
                 39 :RIGHT
                 79 :O})

(def CANVAS-ID "gameCanvas")
(def CANVAS-WIDTH 500)
(def CANVAS-HEIGHT 500)
(def TILE-SIZE 16)

;; entity types
(def player
  {:id :player
   :color "red"
   :health 100
   :attack 5
   :direction :UP})

(def jelly
  {:type :JELLY
   :color "green"
   :health 10
   :attack 5
   :movement :RANDOM})

(def statue
  {:type :STATUE
   :color "grey"
   :health 10
   :attack 5
   :movement :STATIC})

;; map tiles
(def wall
  {:type :WALL
   :color "black"})

(def blank
  {:type :BLANK
   :color "white"})

(def door
  {:type :DOOR
   :color "orange"})

(def closed-chest
  {:type :CLOSED-CHEST
   :color "brown"})

(def opened-chest
  {:type :OPENED-CHEST
   :color "black"})

(def map1
  ["--------------------------------------------------"
   "-@                                              c-"
   "-                                J               -"
   "-                                                -"
   "-           J                         J          -"
   "-                                                -"
   "-                      J                         -"
   "-                                                -"
   "-                                                -"
   "-C          J                         J          -"
   "-----------------------------------------------/--"
   "-C               /S                              -"
   "-                -                               -"
   "-      J         -            J                  -"
   "-                -c                              -"
   "-                -                               -"
   "-                -                               -"
   "-                -                               -"
   "-      J    J    -                               -"
   "-                -                               -"
   "-                -                               -"
   "-                -               J               -"
   "-                -                               -"
   "-                -                               -"
   "-           J    -      J           J            -"
   "-                -                               -"
   "-------/------------------------------------------"
   "-              J                                 -"
   "-                                     SS         -"
   "-                                    SCCS        -"
   "-                      J             SCCS        -"
   "-  J           J                      SS         -"
   "----------------------------/---------------------"
   "-C                         -                    C-"
   "-            J             /     J               -"
   "-                          -               J     -"
   "-                  J       -  J   J              -"
   "-                          -            J        -"
   "-                          -      J        JJ    -"
   "-            J             -               JJ    -"
   "-                          -      J    J   JJ    -"
   "-                          -                  J  -"
   "-     J         J          -      J        J     -"
   "-                          -                     -"
   "-                          - J    J       J      -"
   "-                          -  J  J       J       -"
   "-                          -  J  J   J           -"
   "-     J            J       -      J      J       -"
   "-                          -  J      J           -"
   "--------------------------------------------------"])