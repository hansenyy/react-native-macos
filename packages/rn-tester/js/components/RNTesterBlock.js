/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 * @format
 * @flow
 */

'use strict';

import * as React from 'react';
import {RNTesterThemeContext} from './RNTesterTheme';
import {PlatformColor, StyleSheet, Text, View} from 'react-native';
import {Platform} from 'react-native'; // TODO(macOS GH#774)

type Props = $ReadOnly<{|
  children?: React.Node,
  title?: ?string,
  description?: ?string,
|}>;

/** functional component for generating example blocks */
const RNTesterBlock = (props: Props): React.Node => {
  const {description, title, children} = props;
  const theme = React.useContext(RNTesterThemeContext);
  return (
    <View style={[[styles.container], {borderColor: theme.SeparatorColor}]}>
      <View style={[styles.titleContainer]}>
        <Text style={[styles.titleText]}>{title}</Text>
        <Text
          style={[styles.descriptionText, {marginTop: description ? 10 : 0}]}>
          {description}
        </Text>
      </View>
      <View style={styles.children}>{children}</View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    borderRadius: 0,
    borderWidth: 1,
    ...Platform.select({
      macos: {
        borderColor: PlatformColor('separatorColor'),
        backgroundColor: PlatformColor('windowBackgroundColor'),
      },
      ios: {
        borderColor: PlatformColor('separatorColor'),
        backgroundColor: PlatformColor('tertiarySystemBackgroundColor'),
      },
      default: {
        borderColor: '#d6d7da',
        backgroundColor: '#ffffff',
      },
    }),
    marginTop: 30,
    marginHorizontal: 20,
  },
  titleText: {
    ...Platform.select({
      macos: {
        color: PlatformColor('labelColor'),
      },
      ios: {
        color: PlatformColor('labelColor'),
      },
      default: undefined,
    }),
    fontSize: 18,
    fontWeight: '300',
  },
  titleContainer: {
    ...Platform.select({
      macos: {
        borderBottomColor: PlatformColor('separatorColor'),
        backgroundColor: PlatformColor('controlBackgroundColor'),
      },
      ios: {
        borderBottomColor: PlatformColor('separatorColor'),
        backgroundColor: PlatformColor('tertiarySystemBackgroundColor'),
      },
      default: {
        borderBottomColor: '#d6d7da',
        backgroundColor: '#f6f7f8',
      },
    }),
    paddingHorizontal: 10,
    paddingVertical: 5,
  },
  descriptionText: {
    fontSize: 12,
    opacity: 0.5,
    ...Platform.select({
      macos: {
        color: PlatformColor('secondaryLabelColor'),
      },
      ios: {
        color: PlatformColor('secondaryLabelColor'),
      },
      default: undefined,
    }),
  },
  children: {
    paddingVertical: 10,
    paddingHorizontal: 10,
    margin: 10,
  },
});

module.exports = RNTesterBlock;