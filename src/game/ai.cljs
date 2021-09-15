(ns game.ai
  (:require
   [game.actions :as a]
   [game.monsters.core :as m]))

(defn update-monsters
  "Loop through all monsters and trigger their movement function."
  []
  (doseq [[id monster] (seq @m/monsters)]
    (let [movement (:movement monster)
          [x y] (movement monster)]
      (a/trigger-actions [(m/move-monster id x y)]))))

(defn handle-ai-update
  "Update state for everything not directly controlled by the player."
  []
  (update-monsters))
