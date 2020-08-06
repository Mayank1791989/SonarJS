/*
 * SonarQube JavaScript Plugin
 * Copyright (C) 2011-2020 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
import { RuleTester } from 'eslint';

const ruleTester = new RuleTester({ parserOptions: { ecmaVersion: 2018, sourceType: 'module' } });
import { rule } from '../../src/rules/csrf';

const EXPECTED_MESSAGE_DISABLING = 'Make sure disabling CSRF protection is safe here';

ruleTester.run('Enabling Cross-Origin Resource Sharing is security-sensitive', rule, {
  valid: [],
  invalid: [
    {
      code: `
      var app = express();
      var csrf = require('csurf');
      var csrfProtection = csrf({ cookie: { httpOnly: true, secure:true, ignoreMethods: ["POST", "GET"] }}); // \`POST\` is unsafe, so this protection is not effective
      app.post(..., csrfProtection, ...)  // Noncompliant
      `,
      errors: [
        {
          message: EXPECTED_MESSAGE_DISABLING,
          line: 1,
          endLine: 1,
          column: 22,
          endColumn: 51,
        },
      ],
    },
  ],
});
