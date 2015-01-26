//
//  NSDate+CompareDateOnly.h
//  Gracefull
//
//  Created by moskalenko on 1/8/15.
//  Copyright (c) 2015 NIX. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NSDate (CompareDateOnly)

+ (NSComparisonResult)compareDateOnly:(NSString *)otherDate;

@end
