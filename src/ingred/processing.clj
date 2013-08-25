(ns ingred.processing)

;; from https://gist.github.com/jonpither/4745143#file-gistfile1-txt
(defn pipe
  "Returns a vector containing a sequence that will read from the
queue, and a function that inserts items into the queue.

See http://clj-me.cgrand.net/2010/04/02/pipe-dreams-are-not-necessarily-made-of-promises/"
  [size]
  (let [q (if size
            (java.util.concurrent.LinkedBlockingQueue. size)
            (java.util.concurrent.LinkedBlockingQueue.))
        EOQ (Object.)
        NIL (Object.)
        s (fn s [] (lazy-seq (let [x (.take q)]
                              (when-not (= EOQ x)
                                (cons (when-not (= NIL x) x) (s))))))]
    [(s) (fn ([] (.put q EOQ)) ([x] (.put q (or x NIL))))]))

;; from https://gist.github.com/jonpither/4745343#file-gistfile1-txt
(defn pipe-seq
  "Consumes the col with function f returning a new lazy seq.
The consumption is done in parallel using n-threads backed
by a queue of the specified size. The output sequence is also
backed by a queue of the same given size."
  [f n-threads pipe-size col]
  (let [q (java.util.concurrent.LinkedBlockingQueue. pipe-size)
        finished-feeding (promise)
        latch (java.util.concurrent.CountDownLatch. n-threads)
        [out-seq out-queue] (pipe pipe-size)]

    ;; Feeder thread
    (future
      (doseq [v (remove nil? col)]
        (.put q v))
      (deliver finished-feeding true))

    (dotimes [i n-threads]
      (future (try (loop []
                     (let [v (.poll q 50 java.util.concurrent.TimeUnit/MILLISECONDS)]
                       (when v (out-queue (f v)))
                       (when-not (and (zero? (.size q))
                                      (realized? finished-feeding))
                         (recur))))
                   (finally
                     (.countDown latch)))))

    ;; Supervisor thread
    (future
      (.await latch)
      (out-queue))

    out-seq))

;; a lazy mapcat, from http://clojurian.blogspot.co.uk/2012/11/beware-of-mapcat.html
(defn unfold
  [coll]
  (lazy-seq
   (if (not-empty coll)
     (concat
      (identity (first coll))
      (unfold (rest coll))))))
