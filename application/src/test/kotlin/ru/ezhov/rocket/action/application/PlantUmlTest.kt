package ru.ezhov.rocket.action.application

import org.junit.Ignore

@Ignore // https://plantuml.com/ru/text-encoding
class PlantUmlTest { //    @Test
    //    public void test1() throws IOException {
    //        BrotliLoader.isBrotliAvailable();
    //
    //        String example =
    //                "@startuml\n" +
    //                        "asasasfsaf -> Alice : hello\n" +
    //                        "@enduml";
    //
    //        byte[] input = example.getBytes(StandardCharsets.UTF_8);
    //
    //        // Compress the bytes
    //        byte[] output = new byte[input.length];
    //        Deflater compresser = new Deflater();
    //        compresser.setInput(input);
    //        compresser.finish();
    //        int compressedDataLength = compresser.deflate(output);
    //        compresser.end();
    //
    //        String text = new String(
    //                Base64.getEncoder().encode(new String(output, 0, compressedDataLength).getBytes()),
    //                StandardCharsets.US_ASCII
    //        );
    //
    //        System.out.println(text);
    //    }
    //
    //    @Test
    //    public void test12() throws IOException {
    //        String source = "@startuml\n";
    //        source += "Bob -> Alice : hello\n";
    //        source += "@enduml\n";
    //
    //        SourceStringReader reader = new SourceStringReader(source);
    //        final ByteArrayOutputStream os = new ByteArrayOutputStream();
    //// Write the first image to "os"
    //        String desc = reader.generateImage(os, new FileFormatOption(FileFormat.SVG));
    //        os.close();
    //
    //// The XML is stored into svg
    //        final String svg = new String(os.toByteArray(), Charset.forName("UTF-8"));
    //        System.out.println(svg);
    //    }
    //
    //    @Test
    //    public void test11213() throws IOException {
    //        String source = "@startuml\n";
    //        source += "Bob -> Alice : hello\n";
    //        source += "@enduml\n";
    //        final Transcoder transcoder = TranscoderUtil.getDefaultTranscoder();
    //        final String s = transcoder.encode(source);
    //
    //        System.out.println(s);
    //    }
}