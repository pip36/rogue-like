(ns game.config)

(def input-keys {38 :UP
                 40 :DOWN
                 37 :LEFT
                 39 :RIGHT
                 79 :O})

(def CANVAS-ID "gameCanvas")
(def CANVAS-WIDTH 400)
(def CANVAS-HEIGHT 400)
(def TILE-SIZE 10)

(def tiles-per-screen-x (/ CANVAS-WIDTH TILE-SIZE))
(def tiles-per-screen-y (/ CANVAS-HEIGHT TILE-SIZE))

;; entity types
(def player
  {:id :player
   :display-name "Player Fred"
   :color "red"
   :health 100
   :attack 5
   :direction :UP})

(def jelly
  {:type :JELLY
   :display-name "Jelly"
   :color "green"
   :health 10
   :attack 5
   :movement :RANDOM})

(def statue
  {:type :STATUE
   :display-name "Statue"
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