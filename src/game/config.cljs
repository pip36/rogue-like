(ns game.config)

(def input-keys {38 :UP
                 40 :DOWN
                 37 :LEFT
                 39 :RIGHT
                 79 :O
                 80 :P})

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
   :max-health 100
   :health 100
   :attack 5
   :max-food 100
   :food 100
   :direction :UP})

(def potion
  {:variant :POTION
   :name "Red Potion"
   :quantity 2
   :effects [{:effect :STAT-CHANGE :stat :health :amount 10}]})

(def stat-change-effect
  {:effect :STAT-CHANGE
   :stat :health
   :amount 10})

(def jelly
  {:type :JELLY
   :display-name "Jelly"
   :color "green"
   :max-health 10
   :health 10
   :attack 5
   :movement :RANDOM})

(def statue
  {:type :STATUE
   :display-name "Statue"
   :color "grey"
   :max-health 10
   :health 10
   :attack 5
   :movement :STATIC})

(def soldier
  {:type :SOLDIER
   :display-name "Soldier"
   :color "blue"
   :max-health 30
   :health 30
   :attack 5
   :movement :FOLLOW})

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

(def stairs
  {:type :STAIRS
   :color "purple"})

(def map1
  ["--------------------------------------------------"
   "-@                 +                            c-"
   "-                                J               -"
   "-                                                -"
   "-           J                         J          -"
   "-                                                -"
   "-                      J                         -"
   "-                                                -"
   "-X                                               -"
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