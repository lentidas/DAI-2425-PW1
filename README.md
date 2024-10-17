# shadow 🖼

![Release](https://img.shields.io/github/v/release/lentidas/DAI-2425-PW1?style=for-the-badge) ![License](https://img.shields.io/github/license/lentidas/DAI-2425-PW1?style=for-the-badge)
 ![Docker Build](https://img.shields.io/github/actions/workflow/status/lentidas/DAI-2425-PW1/builder.yaml?style=for-the-badge&logo=docker&label=Docker%20Build)

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
> The program does not store the file extension of the hidden file. Therefore, you must provide the correct file extension when retrieving the hidden file in order to be able to open it correctly.

> [!WARNING]
> Since the storage of the bits of an hidden file overwrites the original bits of the image, the retrieval of the hidden file will set the bits of the image to 0 and not their original value.

### Docker

You can also use our CLI app using the provided Docker image. To do that, you can run the following commands:

```shell
# Pull the image from Docker Hub.
docker pull ghcr.io/lentidas/dai-2425-pw1:latest

# Run the image with the desired command while mounting the current directory to the /data directory inside the container.
# This command hides the file <path-to-file-to-hide> inside the bitmap image <path-to-bitmap-image> and saves the result in <path-to-output-bmp-image>.
docker run --mount type=bind,source="$(pwd)",target=/data <path-to-bitmap-image> <path-to-file-to-hide> hide <path-to-output-bmp-image>
# This command retrieves the hidden file from the bitmap image <path-to-bitmap-image-with-hidden-file> and saves it in <path-to-output-file>.
docker run --mount type=bind,source="$(pwd)",target=/data ghcr.io/lentidas/dai-2425-pw1:latest <path-to-bitmap-image-with-hidden-file> <path-to-output-file> expose
```

The following commands are equivalent to the ones shown on the demonstration section:

```shell
docker run --mount type=bind,source="$(pwd)/examples",target=/data ghcr.io/lentidas/dai-2425-pw1:latest bmp_source.bmp video_to_hide.mp4 hide bmp_with_hidden_video.bmp

docker run --mount type=bind,source="$(pwd)/examples",target=/data ghcr.io/lentidas/dai-2425-pw1:latest bmp_with_hidden_video.bmp video_after_expose.mp4 expose
```

## Demonstration

Here we will show a demonstration of the `hide` and `expose` commands using the files available in the [`examples`](./examples/) directory.

We want to hide this small video ([_source_](https://sample-videos.com/video321/mp4/720/big_buck_bunny_720p_5mb.mp4)):

![Video to hide](https://github.com/user-attachments/assets/5e3d14c7-d8aa-4b4c-8539-167b9e1eb6a3)

Inside this bitmap image (_photo taken by one of the authors_):

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

The documentation of the Java code of our program is published as a GitHub pages alongside this repository [on this address](https://lentidas.github.io/DAI-2425-PW1/).

## Contributing

You are welcome to contribute and improve this project. Below you will find some guidelines to help you get started.

- We try to follow the Semantic Versioning guidelines as much as possible. You can find more information about it [here](https://semver.org/).
- The releases for this project are automated using [Release Please](https://github.com/googleapis/release-please-action).
- As a consequence of the previous 2 points, we use the Conventional Commits specification for our commit messages. You can find more information about it [here](https://www.conventionalcommits.org/).
- We license our code under the GNU General Public License v3.0. You can find it [here](./LICENSE.txt). A copyright header template for the source files is provide [here](./.idea/copyright/DAI_PW1_GNUv3.xml).

### Clone and build the project

We use [Maven](https://maven.apache.org/) to manage our project. The Maven wrapper is versioned alongside with the rest of the code, as well as some project configurations for [IntelliJ IDEA from Jetbrains](https://www.jetbrains.com/idea/). These project files contain run configurations that you can use to run the program from the IDE.

To clone and build the project on the command line, you can use the following commands:

```shell
# Clone the repository.
git clone https://github.com/lentidas/DAI-2425-PW1.git

# Change to the project directory.
cd DAI-2425-PW1

# Modify the code as you wish.

# Check that the code is well formatted...
./mvnw spotless:check

# ...and eventually format it.
./mvnw spotless:apply

# Build the project with the dependencies.
./mvnw dependency:go-offline clean compile package

# Run the program (do not forget to adjust the version accordingly).
java -jar target/shadow-1.0.0.jar --help
```
