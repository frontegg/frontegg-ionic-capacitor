/**
 * Enum representing different log levels.
 */
export enum LogLevel {
  INFO = 'info',
  WARN = 'warn',
  ERROR = 'error',
  NONE = 'none',
}

/**
 * Logger class to handle logging with a configurable log level.
 */
export class Logger {
  private logLevel: LogLevel;

  constructor(logLevel: LogLevel = LogLevel.WARN) {
    this.logLevel = logLevel;
  }

  info(message: string, ...args :any[]): void {
    if (this.shouldLog(LogLevel.INFO)) {
      console.info(message, ...args);
    }
  }

  warn(message: string, ...args :any[]): void {
    if (this.shouldLog(LogLevel.WARN)) {
      console.info(message, ...args);
    }
  }

  error(message: string, ...args :any[]): void {
    if (this.shouldLog(LogLevel.ERROR)) {
      console.info(message, ...args);
    }
  }

  private shouldLog(level: LogLevel): boolean {
    const levels = [ LogLevel.INFO, LogLevel.WARN, LogLevel.ERROR ];
    return levels.indexOf(level) >= levels.indexOf(this.logLevel);
  }
}
