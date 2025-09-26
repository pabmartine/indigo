package com.martinia.indigo.shared.file;

import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class FileServiceImpl implements FileService {
  @Override
  public Path getPath(String path) {
    return Path.of(path);
  }
}
