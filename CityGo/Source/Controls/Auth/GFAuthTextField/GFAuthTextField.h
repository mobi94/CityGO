//
//  GFAuthTextField.h
//  Gracefull
//
//  Created by curly0nion on 10/6/14.
//  Copyright (c) 2014 NIX. All rights reserved.
//

#import "GFTextField.h"

@interface GFAuthTextField : GFTextField

@property(strong, nonatomic, readonly) NSString *identifier;

- (void)setupIdentifier;

@end
