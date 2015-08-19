
package org.rdv.data;

import java.io.IOException;

import javax.imageio.stream.ImageOutputStream;

public interface JPEGConverter {
  void writeJPEG(NumericDataSample sample, ImageOutputStream out)throws IOException;
}
