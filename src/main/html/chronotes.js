if (import.meta.env.PROD) {
  import('../../../target/scala-3.6.3/chronotes-opt/main.js').then(({ Chronotes }) => {
    Chronotes.launch("app");
  });
} else {
  import('../../../target/scala-3.6.3/chronotes-fastopt/main.js').then(({ Chronotes }) => {
    Chronotes.launch("app");
  });
}
