(ns game.actions
  (:require [game.potion-behaviours :as potion]
            [game.state :as s]))

(defn trigger-item-effects [entity-id item-id]
  (doseq [effect (:effects (s/get-item entity-id item-id))]
    (potion/do-effect entity-id effect)))

(defn use-item [entity-id item-id]
  (trigger-item-effects entity-id item-id)
  (s/consume-item entity-id item-id))