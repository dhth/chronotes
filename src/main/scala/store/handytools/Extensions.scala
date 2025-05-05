package store.handytools

object Extensions {
  extension [A, B](a: A) def |>(f: A => B): B = f(a)
}
