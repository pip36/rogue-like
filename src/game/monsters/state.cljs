(ns game.monsters.state
  (:require [game.tile-map :as tile]
            [game.player :as p]))

(defn random-movement
  "Random movement to any surrounding square."
  [monster]
  (let [x (:x monster)
        y (:y monster)
        choices [[x (inc y)] [x (dec y)]
                 [(inc x) y] [(dec x) y]]
        [x2 y2] (get choices (rand-int 4))]
    (cond
      (p/player-at? x2 y2) [x y]
      (tile/is? :BLANK x2 y2) [x2 y2]
      :else [x y])))

(defn static-movement
  "Can't move! Just returns the same position."
  [monster]
  [(:x monster) (:y monster)])

(def jelly
  {:type :JELLY
   :color "darkred"
   :movement random-movement})

(def statue
  {:type :STATUE
   :color "grey"
   :movement static-movement})

(defn create-jelly [jelly-data]
  (merge jelly-data jelly))

(defn create-statue [statue-data]
  (merge statue-data statue))

(def monsters (atom  {1 (create-jelly {:id 1 :x 13 :y 13})
                      2 (create-jelly {:id 2 :x 1 :y 1})
                      3 (create-jelly {:id 3 :x 6 :y 6})
                      4 (create-statue {:id 4 :x 9 :y 10})}))



