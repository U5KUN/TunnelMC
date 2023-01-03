# TunnelMC

TunnelMCはJava EditionのプレイヤーがBedrock Editionのサーバーに入り、プレイすることを可能にします。

# 機能
TunnelMCは[Fabric Mod](https://fabricmc.net/)です。私たちは、Minecraft Bedrockサーバーへの接続を開き、送信パケットと受信パケットを変換し、Java Edition、Bedrock Editionの双方で読み取れるようにしています。

# なぜプロキシではなくFabric Modなのか
えぇ、私たちはFabricを愛しています❤️(翻訳者:ここでいう"私たち"は、おそらくこのプロジェクトを立ち上げた、[THEREALWWEFAN231](https://github.com/THEREALWWEFAN231)、[Camotoy](https://github.com/Camotoy)、[JustTalDevelops](https://github.com/JustTalDevelops)、[Cy4Shot](https://github.com/Cy4Shot)の4人のことだと思います)。プロキシではなくModにすることで、より多くのことができるようになります。例えば、スキンは[Minecraft.net](https://minecraft.net/)の代わりに、Bedrockサーバーから読み込みます。これは、何らかのModがなければ不可能です。また、*技術的*にはMinecraft Java Editionにはない、エモートなどの機能を追加することも可能です。きっと、エモートを追加することはありませんが、やろうと思えば*できます*。
# 何が追加されたのか
ええ、それは正しい質問ではありません。私たちは最近開発を開始したばかり(書かれた当時)で、本当の問題は何を追加したかということです。
- オフラインサーバー認証(bedrock専用サーバーでは機能しませんが、nukkitでは機能します)
- 基本チャンク変換
- ブロック変換([ガイザーマッピング](https://github.com/GeyserMC/mappings)に感謝。まだ、少し作業が必要ですが、多くはガイザーマッピングを利用すればすぐに終わりそうです。
- スキン(一般的には機能していますが、レイヤーは機能していないようです)
- チャット
- 泳ぎのアニメーション

# 貢献する
このプロジェクトを支援したい、または支援を試みたいのですが、どうすればよいのですか? プロジェクトのセットアップは、Eclipseの他の[Fabric Mod](https://fabricmc.net/)と同じです。gradlew genSourcesコマンドを実行してから、gradlew eclipseを実行し、それを既存のプロジェクトとしてEclipseにインポートする必要があります。 別のIDEを使用している場合は、[Fabric Wiki](https://fabricmc.net/wiki/tutorial:setup)を参照してください。

また、このスタイルでコーディング(コードを記入すること)していただけると幸いです。
```java
if(x) {
  doSomething();
}
x.forEach(new Consumer<X>() {

  @Override
  public void accept(X x) {
    doSomething();
  }
});
```
むしろ、その後
```java
if(x)
  doSomething();
x.forEach((x) -> {
  doSomething();
});
```
また、xbox Live/aipなどに関する知識があれば、xbox認証を追加したり、招待からワールドに参加してみてもいいでしょう。😎

# クレジット
このプロジェクトは、いくつかのオープンソースプロジェクトの引用をしなければ作成は不可能です。逆変換するために動作を調べたり、コードを調べて動作を確認したり、コードを少しコピーしたりする必要があります。 私たちはこれらすべてのプロジェクトをここに示します。
- [Protocol](https://github.com/CloudburstMC/Protocol)
- [Nukkit](https://github.com/CloudburstMC/Nukkit)
- [Geyser](https://github.com/GeyserMC/Geyser)
- [gophertunnel](https://github.com/Sandertv/gophertunnel)

# 使用方法
現在、開発中であり、まだ多くの機能が追加されていません。(翻訳者:どうしてもやりたかったら公式Discordに来るといいよ!)

# [Discord](https://discord.gg/qH6GqxW)
(翻訳者:絶対Discord来たほうがいい)Discordでは、TunnelMCやその代わりになりそうなプロジェクトなどの情報を入手できます。また、このプロジェクトに協力したい場合はぜひ来てください。crakin.kk(翻訳者:crakin.kkってなんや!って翻訳してて思ったw)の内容を確認できます。

# 画像
これは、Java EditionでBedrock Editionのサーバーに参加している時の画像です。
![](/pictures/JavaEdition.png)
これは、Bedrock EditionのプレイヤーからJava Editionのプレイヤーを見た時の画像です。
![](/pictures/Windows10Edition.png)

# 開発者のGitHubプロフィールへのリンク
[THEREALWWEFAN231](https://github.com/THEREALWWEFAN231)
[Camotoy](https://github.com/Camotoy)
[JustTalDevelops](https://github.com/JustTalDevelops)
[Cy4Shot](https://github.com/Cy4Shot)
[Flonja](https://github.com/Flonja)

# 翻訳者から
(書かれた当時)は翻訳した当時では無く、原文が書かれた当時のことをさしています。
(書かれた当時)や(翻訳者:)などは、[原文](https://github.com/THEREALWWEFAN231/TunnelMC)や[私がフォークしたプロジェクト](https://github.com/Flonja/TunnelMC)には書かれていない内容で、私が勝手に追加したものです。
また、一部[Google翻訳](https://translate.google.com/)を使用して翻訳しました。
このプロジェクトは、1.16で開発が止まっているため、1.19に更新されている[こちらのプロジェクト](https://github.com/Flonja/TunnelMC)からフォークしました。
私には、Javaの知識はほとんど無いのですが、頑張って最新版に対応させたいので協力者を募集しています。協力してくれる方は、ぜひDiscordの方に来てU5KUNを呼んでください。
