const suffix = import.meta.env.PROD ? "opt" : "fastopt";
import(`../../../target/scala-3.7.0/chronotes-${suffix}/main.js`).then(
	({ Chronotes }) => {
		Chronotes.launch("app");
	},
);
