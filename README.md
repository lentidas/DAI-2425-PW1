# shadow 🖼

![Release](https://img.shields.io/github/v/release/lentidas/DAI-2425-PW1?style=for-the-badge) ![License](https://img.shields.io/github/license/lentidas/DAI-2425-PW1?style=for-the-badge)
 ![Build Status](https://img.shields.io/github/actions/workflow/status/lentidas/DAI-2425-PW1/build.yaml?style=for-the-badge)

A Java CLI application that allows you to hide and retrieve files hidden in bitmap images 🖼.

> [!NOTE]
> This program was written as our first practical work during the [DAI course](https://github.com/heig-vd-dai-course/heig-vd-dai-course/tree/main) of 2024 at the [HEIG-VD](https://heig-vd.ch).

## Authors

- Pedro Alves da Silva ([@PedroAS7](https://github.com/PedroAS7))
- Gonçalo Carvalheiro Heleno ([@lentidas](https://github.com/lentidas))

## Usage

Shadow is a CLI application that allows you to hide and retrieve files hidden in bitmap images. It has two main commands: `hide` and `expose`.

> [!NOTE]
> Ensure you have Java 21 or later installed on your machine.

### `hide`

The `hide` command allows you to hide any file inside a bitmap image. The syntax is as follows:

```shell
java -jar shadow-1.0.0.jar <path-to-bitmap-image> <path-to-file-to-hide> hide <path-to-output-bmp-image>
```

Where `<path-to-bitmap-image>` is the path to the bitmap image,`<path-to-file-to-hide>` is the path to the file you want to hide and `<path-to-output-bmp-image>` is the path where the bitmap image with the hidden content will be available.

You can also specify how many bits per byte of the image you want to use to hide the file. The default value is 1. To specify the number of bits per byte, you can use the `-b`/`--bits-per-byte` flag:

```shell
java -jar shadow-1.0.0.jar <path-to-bitmap-image> <path-to-file-to-hide> hide <path-to-output-bmp-image> -b <number-of-bits-per-byte>
```

> [!NOTE]
> The number of bits per byte must be between 1 and 8 and a power of 2.

> [!WARNING]
> The more bits per byte you use, the more noticeable the changes in the image will be, since the changes in the colors of the pixels will be more significant.

If a file with the same name as the output file already exists, the program will ask you if you want to overwrite it. You can do that by re-executing the command with the `-f`/`--force` flag before the subcommand `hide`:

```shell
java -jar shadow-1.0.0.jar <path-to-bitmap-image> <path-to-file-to-hide> --force hide <path-to-output-bmp-image>
```

### `expose`

The `expose` command allows you to retrieve the file hidden in a bitmap image. The syntax is as follows:

```shell
java -jar shadow-1.0.0.jar <path-to-bitmap-image-with-hidden-file> <path-to-output-file> expose
```

Where `<path-to-bitmap-image-with-hidden-file>` is the path to the bitmap image with the hidden file and `<path-to-output-file>` is the path where the hidden file will be saved.

If the bitmap image provided does not contain a hidden file, the program will return an error message. Also, if the output file already exists, the program will ask you if you want to overwrite it. You can do that by re-executing the command with the `-f`/`--force` flag before the subcommand `expose`:

```shell
java -jar shadow-1.0.0.jar <path-to-bitmap-image-with-hidden-file> <path-to-output-file> --force expose
```

> [!IMPORTANT]
> Since the storage of the bits of an hidden file overwrites the original bits of the image, the retrieval of the hidden file will set the bits of the image to 0 and not their original value.

<!-- TODO Add Docker examples after first release -->

## Demonstration

Here we will show a demonstration of the `hide` and `expose` commands using the files available in the [`examples`](./examples/) directory.

We want to hide this small video:

![Video to hide](https://github.com/user-attachments/assets/5e3d14c7-d8aa-4b4c-8539-167b9e1eb6a3)

Inside this bitmap image:

![Bitmap image](./examples/bmp_source.bmp)

We do that by running the following command:

```shell
java -jar shadow-1.0.0.jar ./examples/bmp_source.bmp ./examples/video_to_hide.mp4 hide ./examples/bmp_with_hidden_video.bmp
```

This is the image we obtain:

![Bitmap image with hidden video](./examples/bmp_with_hidden_video.bmp)

As you can see, the image looks the same as the original one. However, it now contains the hidden video!

We then recovered the video with the following command:

```shell
java -jar shadow-1.0.0.jar ./examples/bmp_with_hidden_video.bmp ./examples/video_after_expose.mp4 expose
```

And this is the video we obtained:

![Video after expose](https://github.com/user-attachments/assets/44e07f68-eb69-4f2b-af17-9ae0bc8995df)

Finally, we can demonstrate that the original video and the video obtained after the expose command are the same by comparing their hashes (you can also see that the hashes of the bitmap images are different):

```shell
$ openssl dgst -sha256 ./examples/*

SHA2-256(./examples/bmp_source.bmp)= cbf55a3449df1b117832d8cf04153fd87d9e4e39acd6cfccaa2dd15d7849a806
SHA2-256(./examples/bmp_with_hidden_video.bmp)= 502e039c157166f37848121b2ad37222b7a7f18cb236136c1b1a6cd0f5bc502f
SHA2-256(./examples/video_after_expose.mp4)= 771965e3f757c3861cc5a02f454c17b52c128a45d7630938f7f22b1f87738238
SHA2-256(./examples/video_to_hide.mp4)= 771965e3f757c3861cc5a02f454c17b52c128a45d7630938f7f22b1f87738238
```

## Documentation

// TODO Point to the Javadoc documentation \

## Contributing

// TODO Explain the release process \
// TODO Explain the Conventional commits \
