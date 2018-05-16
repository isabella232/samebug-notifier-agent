# Samebug notifier agent

Intercept and report runtime crashes to a remote server.

## Build

```
mvn assembly:assembly
```

## Usage


```
java -javaagent:/tmp/notifier-agent-0.1.0.jar=crashMonitorUrl=http://localhost:9000 MyApp
```

## Agent parameters

- `crashMonitorUrl`: an http url where the reports are sent. Ideally it is the address where the
[crash monitor](https://github.com/samebug/samebug-crash-monitor) server listens.
