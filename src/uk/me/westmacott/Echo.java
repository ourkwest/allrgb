package uk.me.westmacott;

import java.awt.*;

public interface Echo {

  void echo(int[][] canvas, Point point);

  static Echo NoopEcho() {
    return (canvas, point) -> { /* No Op */ };
  }

}
