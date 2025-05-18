if (import.meta.env.PROD) {
  import('../../../target/scala-3.7.0/chronotes-opt/main.js').then(({ Chronotes }) => {
    Chronotes.launch("app");
  });
} else {
  import('../../../target/scala-3.7.0/chronotes-fastopt/main.js').then(({ Chronotes }) => {
    Chronotes.launch("app");
  });
}
