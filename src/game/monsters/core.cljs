(ns game.monsters.core
  (:require
   [game.tile-map :as tm]
   [game.canvas :as canvas]
   [game.actions :as a]
   [game.monsters.state :as s]))

;; ACTIONS
(defn move-monster [id x y]
  {:type :MOVE_MONSTER :payload {:id id :x x :y y}})

(defmethod a/handle :MOVE_MONSTER [action]
  (swap! s/monsters conj {(-> action :payload :id) (merge (get @s/monsters (-> action :payload :id)) {:x (-> action :payload :x)
                                                                                                      :y (-> action :payload :y)})}))

;; RENDER
(defn render-monster [monster]
  (canvas/draw-rect
   (* tm/TILE-SIZE (:x monster))
   (* tm/TILE-SIZE (:y monster))
   tm/TILE-SIZE tm/TILE-SIZE
   (:color monster)))


(defn render-monsters []
  (doseq [[_ monster] (seq @s/monsters)]
    (render-monster monster)))

(defn all-monsters []
  (map last @s/monsters))

(defn get-monster-at [x y]
  (some
   #(and (= (:x %) x) (= (:y %) y) %)
   (all-monsters)))