(ns game.monsters.core
  (:require
   [game.tile-map :as tm]
   [game.canvas :as canvas]
   [game.actions :as a]
   [game.monsters.types :as monsters]))

(defn create-jelly [jelly-data]
  (merge jelly-data monsters/jelly))

(defn create-statue [statue-data]
  (merge statue-data monsters/statue))

;; STATE
(def monsters (atom  {1 (create-jelly {:id 1 :x 13 :y 13})
                      2 (create-jelly {:id 2 :x 1 :y 1})
                      3 (create-jelly {:id 3 :x 6 :y 6})
                      4 (create-statue {:id 4 :x 9 :y 10})}))

;; ACTIONS
(defn move-monster [id x y]
  {:type :MOVE_MONSTER :payload {:id id :x x :y y}})

(defmethod a/handle :MOVE_MONSTER [action]
  (swap! monsters conj {(-> action :payload :id) (merge (get @monsters (-> action :payload :id)) {:x (-> action :payload :x)
                                                                                                  :y (-> action :payload :y)})}))

;; RENDER
(defn render-monster [monster]
  (canvas/draw-rect
   (* tm/TILE-SIZE (:x monster))
   (* tm/TILE-SIZE (:y monster))
   tm/TILE-SIZE tm/TILE-SIZE
   (:color monster)))


(defn render-monsters []
  (doseq [[_ monster] (seq @monsters)]
    (render-monster monster)))
